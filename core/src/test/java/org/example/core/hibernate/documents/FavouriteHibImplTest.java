package org.example.core.hibernate.documents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.getting.favourites.FavouriteCountByGoodDto;
import org.example.core.dto.getting.favourites.FavouriteGetForUserDto;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.models.*;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.RoleTypes;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        FavouriteHibImpl.class,
        GoodHibImpl.class,
        UserHibImpl.class,
        UnitHibImpl.class,
})
@Transactional
public class FavouriteHibImplTest {
    private static final Logger logger  = LogManager.getLogger(FavouriteHibImplTest.class);
    @Autowired
    UnitHibImpl unitHib;

    @Autowired
    UserHibImpl userHib;

    @Autowired
    GoodHibImpl goodHib;

    @Autowired
    FavouriteHibImpl favouriteHib;

    @Autowired
    SessionFactory sessionFactory;

    private Unit unit;

    private Good createGood(String name){
        Good good = new Good();
        good.setUnit(unit);
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        good.setName(name);
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        good.setRate(1d);
        goodHib.save(good, logger);
        return good;
    }

    private User createUser(String login, String username){
        sessionFactory.getCurrentSession()
                .createNativeQuery("INSERT INTO roles (name) VALUES('MIN_USER') ON CONFLICT DO NOTHING")
                .executeUpdate();

        Role role =  sessionFactory.getCurrentSession().createQuery("FROM Role WHERE name = :name", Role.class)
                .setParameter("name", RoleTypes.MIN_USER).uniqueResult();


        User user = new User();
        user.setLogin(login);
        user.setUsername(username);
        user.setUpdatedAt(Instant.now());
        user.setCreatedAt(Instant.now());
        user.setPassword("pas");
        user.setNonLocked(true);
        user.setRole(role);
        userHib.save(user, logger);
        return user;
    }

    private Favourite createFavourite(Good good, User user){
        Favourite fav = new Favourite();
        fav.setCreatedAt(Instant.now());
        fav.setGood(good);
        fav.setUser(user);
        favouriteHib.save(fav, logger);
        return fav;

    }

    @BeforeEach
    void setUp(){
        unit = new Unit();
        unit.setFullName("full");
        unit.setShortName("short");
        unitHib.save(unit, logger);
    }

    @Test
    @Tag("positive")
    @DisplayName("removeAndFindByUserIdAndGoodId")
    public void removeAndFindByUserIdAndGoodIdPureVersion(){
        Good good = createGood("name");
        User user = createUser("l", "u");
        Favourite favourite = createFavourite(good, user);

        Favourite found = favouriteHib.findByUserIdAndGoodIdPureVersion(user.getId(), good.getId());

        Assertions.assertNotNull(found);
        Assertions.assertNotNull(found.getGood());
        Assertions.assertEquals(good.getId(), found.getGood().getId());

        favouriteHib.remove(favourite);

        Assertions.assertNull(favouriteHib.findByUserIdAndGoodIdPureVersion(user.getId(), good.getId()));

    }

    @Test
    @Tag("positive")
    @DisplayName("removeAndFindByUserIdAndGoodId")
    public void findByUserIdAndGoodIdPureVersionIfAllNotExist(){
        Good good = createGood("name");
        User user = createUser("l", "u");
        createFavourite(good, user);

        Favourite found = favouriteHib.findByUserIdAndGoodIdPureVersion(900L, 9000L);
        Assertions.assertNull(found);

    }

    @Test
    @Tag("positive")
    @DisplayName("findAllByUserIfGoodExist")
    public void findAllByUserIfGoodExist(){
        Good good = createGood("name");
        User user1 = createUser("log1", "user1");
        User user = createUser("log", "user");
        createFavourite(good, user);
        createFavourite(good, user1);

        List<FavouriteGetForUserDto> found = favouriteHib.findAllByUser(user.getId());
        Assertions.assertNotNull(found);
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(good.getId(), found.get(0).getGoodId());

    }

    @Test
    @Tag("positive")
    @DisplayName("findAllByUserIfSuccess")
    public void findAllByUserIfGoodDoesNotExist(){

        User user = createUser("log", "user");
        List<FavouriteGetForUserDto> found = favouriteHib.findAllByUser(user.getId());
        Assertions.assertNotNull(found);
        Assertions.assertEquals(0,found.size());


    }

    @Test
    @Tag("positive")
    @DisplayName("findAllByUserIfUserDoesNotExist")
    public void findAllByUserIfUserDoesNotExist(){
        Good good = createGood("name");
        User user = createUser("log", "user");
        createFavourite(good, user);
        List<FavouriteGetForUserDto> found = favouriteHib.findAllByUser(999L);
        Assertions.assertNotNull(found);
        Assertions.assertEquals(0,found.size());

    }

    @Test
    @Tag("positive")
    @DisplayName("countByGoodIdIfExist")
    public void countByGoodIdIfExist(){
        Good good = createGood("name");
        Good good1 = createGood("name1");

        User user = createUser("log", "user");
        User user1 = createUser("log1", "user1");
        User user2 = createUser("log2", "user2");

        createFavourite(good, user);
        createFavourite(good, user1);
        createFavourite(good1, user2);

        FavouriteCountByGoodDto found = favouriteHib.countByGoodId(good.getId());
        Assertions.assertNotNull(found);
        Assertions.assertEquals(2, found.getCountFavourites());
        Assertions.assertEquals(good.getId(), found.getGoodId());
        Assertions.assertEquals(good.getName(), found.getGoodName());

    }

    @Test
    @Tag("positive")
    @DisplayName("countByGoodIdIfExist")
    public void countByGoodIdIfNotFound(){
        Good good = createGood("name");
        Good good1 = createGood("name1");

        User user = createUser("log", "user");
        User user1 = createUser("log1", "user1");
        User user2 = createUser("log2", "user2");

        createFavourite(good1, user);
        createFavourite(good1, user1);
        createFavourite(good1, user2);

        FavouriteCountByGoodDto found = favouriteHib.countByGoodId(good.getId());
        Assertions.assertNull(found);

    }

    @Test
    @Tag("positive")
    @DisplayName("countByGoodIdIfGoodDoesNotExist")
    public void countByGoodIdIfGoodDoesNotExist(){
        Good good1 = createGood("name1");

        User user = createUser("log", "user");
        User user1 = createUser("log1", "user1");
        User user2 = createUser("log2", "user2");

        createFavourite(good1, user);
        createFavourite(good1, user1);
        createFavourite(good1, user2);

        FavouriteCountByGoodDto found = favouriteHib.countByGoodId(1900L);
        Assertions.assertNull(found);
    }
}
