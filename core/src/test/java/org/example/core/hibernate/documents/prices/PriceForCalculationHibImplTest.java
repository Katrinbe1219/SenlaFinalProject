package org.example.core.hibernate.documents.prices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.creating.ReviewCreateDto;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.hibernate.dictionaries.TagHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.documents.ReviewHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.models.*;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.RoleTypes;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        PriceForCalculationHibImpl.class,
        GoodHibImpl.class,
        ReviewHibImpl.class,
        UnitHibImpl.class,
        UserHibImpl.class,
})
@Transactional
public class PriceForCalculationHibImplTest {

    @Autowired
    UnitHibImpl unitHib;

    @Autowired
    GoodHibImpl goodHib;

    @Autowired
    ReviewHibImpl reviewHib;

    @Autowired
    PriceForCalculationHibImpl priceForCalculationHib;
    @Autowired
    UserHibImpl userHib;

    private Unit unit;
    private Good good;
    private User user;
    private Role role;

    @Autowired
    SessionFactory sessionFactory;

    private static final Logger logger = LogManager.getLogger(PriceHibForExportTest.class);


    public Good createGood(String name, Double rate, Unit unit) {
        Good good = new Good();
        good.setName(name);
        good.setUnit(unit);
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        good.setRate(rate);
        goodHib.save(good, logger);
        return good;
    }

    public Unit createUnit() {
        Unit unit = new Unit();
        unit.setShortName("short");
        unit.setFullName("full");
        unitHib.save(unit, logger);
        return unit;
    }

    @BeforeEach
    void setUp() {
        unit = createUnit();
        good = createGood("TestGood", 4.0, unit);

        sessionFactory.getCurrentSession()
                .createNativeQuery("INSERT INTO roles (name) VALUES('MIN_USER') ON CONFLICT DO NOTHING")
                .executeUpdate();
        sessionFactory.getCurrentSession()
                .createNativeQuery("INSERT INTO roles (name) VALUES('MODERATOR') ON CONFLICT DO NOTHING")
                .executeUpdate();

        role = sessionFactory.getCurrentSession()
                .createQuery("FROM Role WHERE name = :name", Role.class)
                .setParameter("name", RoleTypes.MIN_USER).uniqueResult();

        user = new User();
        user.setLogin("uLog");
        user.setUsername("username");
        user.setPassword("pass");
        user.setRole(role);
        user.setNonLocked(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userHib.save(user, logger);

    }

    private Review createReview(User u, Good g, int rate) {
        ReviewCreateDto dto = new ReviewCreateDto();
        dto.setRate(rate);
        dto.setReview("review text");

        Review review = reviewHib.createReview(dto, g, u);
        sessionFactory.getCurrentSession().flush();
        return review;
    }

    private User createUser(String login, String username){
        User user2 = new User();
        user2.setLogin(login);
        user2.setUsername(username);
        user2.setPassword("pass");
        user2.setRole(user.getRole());
        user2.setNonLocked(true);
        user2.setCreatedAt(Instant.now());
        user2.setUpdatedAt(Instant.now());
        user2.setRole(role);

        userHib.save(user2, logger);
        return user2;
    }

    @Test
    @DisplayName("recalculateForGoodIfNoGood")
    void recalculateForGoodIfNoGood(){
        Double res = priceForCalculationHib.recalculateForGood(10000L);
        Assertions.assertNull(res);
    }

    @Test
    @DisplayName("recalculateForGoodIfNoGood")
    void recalculateForGoodIfGoodButNotReview(){

        Double res = priceForCalculationHib.recalculateForGood(good.getId());
        Assertions.assertNotNull(res);
    }

    @Test
    @DisplayName("recalculateForGoodIfGoodButIfBlocked")
    void recalculateForGoodIfGoodButIfBlocked(){
        Review r1= createReview(user, good, 4);
        User user1 = createUser("l12", "useruser");
        Review r2 = createReview(user1, good, 4);

        r1.setBlocked(true);
        r2.setBlocked(true);
        sessionFactory.getCurrentSession().flush();

        Double res = priceForCalculationHib.recalculateForGood(good.getId());
        Assertions.assertEquals(0, Double.valueOf(0).compareTo(res));
    }

    @Test
    @DisplayName("recalculateForGoodIfGoodButIfNotBlocked")
    void recalculateForGoodIfGoodButIfNotBlocked(){
        createReview(user, good, 4);
        User user1 = createUser("l12", "useruser");
        createReview(user1, good, 3);

        Double res = priceForCalculationHib.recalculateForGood(good.getId());
        Assertions.assertNotNull(res);
    }


    @Test
    @DisplayName("recalculateForGoodIfGoodButIfNotBlocked")
    void recalculateForAllGoodIfGoodButIfNotBlocked(){
        createReview(user, good, 2);
        User user1 = createUser("l12", "useruser");
        createReview(user1, good, 2);

        Good good1 = createGood("new", 2d, unit);

        createReview(user1, good1, 5);
        createReview(user, good1, 5);

        //чтобы в кеше не осталось старое значение, ведь потом обнолвение через нативный код идет
        sessionFactory.getCurrentSession().clear();

        Map<Long, Good> res = priceForCalculationHib.recalculateForAllGoods(List.of(good.getId(), good1.getId()));
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();


        Good check1 = sessionFactory.getCurrentSession().get(Good.class, good.getId());
        Assertions.assertEquals(0, check1.getRate().compareTo(res.get(good.getId()).getRate()));

        Good check2 = sessionFactory.getCurrentSession().get(Good.class, good1.getId());
        Assertions.assertEquals(0, check2.getRate().compareTo(res.get(good1.getId()).getRate()));

    }
}
