package org.example.core.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.documents.prices.PriceHibImpl;
import org.example.core.hibernate.documents.subscriptions.AvailabilitySubHib;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.models.*;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.RoleTypes;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        CheckingMultiExistenceHib.class,
        ShopHibImpl.class,
        GoodHibImpl.class,
        UserHibImpl.class,
        PriceHibImpl.class,
        DistrictHibImpl.class,
        UnitHibImpl.class,

        AvailabilitySubHib.class
})
@Transactional
public class CheckingMultiExistenceHibTest {

    @Autowired
    CheckingMultiExistenceHib checkHib;
    @Autowired
    ShopHibImpl shopHib;
    @Autowired
    GoodHibImpl goodHib;
    @Autowired
    UserHibImpl userHib;
    @Autowired
    DistrictHibImpl districtHib;
    @Autowired
    PriceHibImpl priceHib;
    @Autowired
    UnitHibImpl unitHib;
    @Autowired
    AvailabilitySubHib availabilitySubHib;

    private Shop shop;
    private Good good;
    private User user;
    @Autowired
    SessionFactory sessionFactory;

    private static final Logger logger = LogManager.getLogger(CheckingMultiExistenceHibTest.class);

    @BeforeEach
    void   setUp() {
        District dis = new District();
        dis.setName("vorsh");
        districtHib.save(dis, logger);

        shop = new Shop();
        shop.setName("TestShop");
        shop.setAddress("TestAddress");
        shop.setDistrict(dis);
        shopHib.save(shop, logger);

        Unit unit= new Unit();
        unit.setShortName("short");
        unit.setFullName("fyll");
        unitHib.save(unit, logger);

        good = new Good();
        good.setName("TestGood");
        good.setUnit(unit);
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        goodHib.save(good, logger);


        Session session = sessionFactory.getCurrentSession();

        session.createNativeQuery("INSERT INTO roles (name) VALUES('MIN_USER') ON CONFLICT DO NOTHING")
                .executeUpdate();
        session.createNativeQuery("INSERT INTO roles (name) VALUES('MAX_USER') ON CONFLICT DO NOTHING")
                .executeUpdate();
        session
                .createNativeQuery("INSERT INTO roles (name) VALUES('ANALYST')")
                .executeUpdate();



        Role role = session
                .createQuery("FROM Role WHERE name = :name", Role.class)
                .setParameter("name", RoleTypes.ANALYST)
                .uniqueResult();


        user = new User();
        user.setLogin("testlogin");
        user.setUsername("testuser");
        user.setRole(role);
        user.setPassword("pass");
        user.setCreatedAt(Instant.now());
        user.setNonLocked(true);
        user.setUpdatedAt(Instant.now());
        userHib.save(user, logger);
    }

    @Test
    @DisplayName("checkShopAndGoodByIdsIfShopDoesNotExist")
    @Tag("positive")
    void checkShopAndGoodByIdsIfNothingFound(){
        Map<String, Boolean> result = checkHib.checkShopAndGoodByIds(123123L, 123123L, 123123123L);
        Assertions.assertFalse(result.getOrDefault("goodId", null));
        Assertions.assertFalse(result.getOrDefault("shopId", null));
        Assertions.assertFalse(result.getOrDefault("priceId", null));
        Assertions.assertFalse(result.getOrDefault("sub", null));
    }

    @Test
    @DisplayName("checkShopAndGoodByIdsIfNotPriceAndAvailSubs")
    @Tag("positive")
    void checkShopAndGoodByIdsIfNotPriceAndAvailSubs(){
        Map<String, Boolean> result = checkHib.checkShopAndGoodByIds(good.getId(), shop.getId(), user.getId());
        Assertions.assertTrue(result.getOrDefault("goodId", null));
        Assertions.assertTrue(result.getOrDefault("shopId", null));
        Assertions.assertFalse(result.getOrDefault("priceId", null));
        Assertions.assertFalse(result.getOrDefault("sub", null));
    }

    @Test
    @DisplayName("checkShopAndGoodByIdsIfPriceExists")
    @Tag("positive")
    void checkShopAndGoodByIdsIfPriceExists(){

        Price price = new Price();
        price.setGood(good);
        price.setShop(shop);
        price.setPrice(BigDecimal.valueOf(100));
        price.setValidFrom(Instant.now());
        price.setValidTo(null);
        priceHib.save(price, logger);

        Map<String, Boolean> result = checkHib.checkShopAndGoodByIds(good.getId(), shop.getId(), user.getId());
        Assertions.assertTrue(result.getOrDefault("goodId", null));
        Assertions.assertTrue(result.getOrDefault("shopId", null));
        Assertions.assertTrue(result.getOrDefault("priceId", null));
        Assertions.assertFalse(result.getOrDefault("sub", null));
    }

    @Test
    @DisplayName("checkShopAndGoodByIdsIfPriceOutdated")
    @Tag("positive")
    void checkShopAndGoodByIdsIfPriceOutdated(){

        Price price = new Price();
        price.setGood(good);
        price.setShop(shop);
        price.setPrice(BigDecimal.valueOf(100));
        price.setValidFrom(Instant.now().minus(10, ChronoUnit.DAYS));
        price.setValidTo(Instant.now().minus(1, ChronoUnit.DAYS));
        priceHib.save(price, logger);

        Map<String, Boolean> result = checkHib.checkShopAndGoodByIds(good.getId(), shop.getId(), user.getId());
        Assertions.assertTrue(result.getOrDefault("goodId", null));
        Assertions.assertTrue(result.getOrDefault("shopId", null));
        Assertions.assertFalse(result.getOrDefault("priceId", null));
        Assertions.assertFalse(result.getOrDefault("sub", null));
    }

    @Test
    @Tag("positive")
    @DisplayName("checkIfSubExists")
    void checkIfSubExists() {
        AvailabilitySubscription sub = new AvailabilitySubscription();
        sub.setGood(good);
        sub.setShop(shop);
        sub.setUser(user);
        sub.setCreatedAt(Instant.now());
        availabilitySubHib.save(sub, logger);

        Map<String, Boolean> result = checkHib.checkShopAndGoodByIds(
                good.getId(), shop.getId(), user.getId()
        );

        Assertions.assertTrue(result.get("sub"));
        Assertions.assertFalse(result.get("priceId"));
    }

    @Test
    @Tag("positive")
    @DisplayName("checkIfEverythingExists")
    void checkIfEverythingExists() {
        Price price = new Price();
        price.setGood(good);
        price.setShop(shop);
        price.setPrice(BigDecimal.valueOf(100));
        price.setValidFrom(Instant.now());
        price.setValidTo(null);
        priceHib.save(price, logger);

        AvailabilitySubscription sub = new AvailabilitySubscription();
        sub.setGood(good);
        sub.setShop(shop);
        sub.setUser(user);
        sub.setCreatedAt(Instant.now());
        availabilitySubHib.save(sub, logger);

        Map<String, Boolean> result = checkHib.checkShopAndGoodByIds(
                good.getId(), shop.getId(), user.getId()
        );

        Assertions.assertTrue(result.get("goodId"));
        Assertions.assertTrue(result.get("shopId"));
        Assertions.assertTrue(result.get("priceId"));
        Assertions.assertTrue(result.get("sub"));
    }
}
