package org.example.core.hibernate.documents.subscriptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.hibernate.base_settings.filters.subscriptions.AvailabilitySubFilter;
import org.example.core.hibernate.base_settings.sorting_types.AvailabilitySubSortType;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        AvailabilitySubHib.class,
        GoodHibImpl.class,
        UnitHibImpl.class,
        UserHibImpl.class,
        ShopHibImpl.class,
        DistrictHibImpl.class,

})
@Transactional
class AvailabilitySubHibTest {

    @Autowired
    AvailabilitySubHib availabilitySubHib;

    @Autowired
    SessionFactory sessionFactory;
    @Autowired
    UnitHibImpl unitHib;
    @Autowired
    GoodHibImpl goodHib;

    private static final Logger logger = LogManager.getLogger(AvailabilitySubHibTest.class);

    private Unit unit;
    private Good good;
    private User user;
    private Shop shop;
    private Role role;
    private District district;


    @Autowired
    private UserHibImpl userHibImpl;
    @Autowired
    private DistrictHibImpl districtHibImpl;
    @Autowired
    private ShopHibImpl shopHibImpl;

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

    private Shop createShop(String name, String address){
        Shop shop = new Shop();
        shop.setName(name);
        shop.setAddress(address);
        shop.setDistrict(district);
        shopHibImpl.save(shop, logger);
        return shop;
    }

    @BeforeEach
    void setUp() {
        unit = createUnit();
        good = createGood("TestGood", 4.0, unit);

        sessionFactory.getCurrentSession()
                .createNativeQuery("INSERT INTO roles (name) VALUES('MIN_USER') ON CONFLICT DO NOTHING")
                .executeUpdate();
        role = sessionFactory.getCurrentSession()
                .createQuery("FROM Role WHERE name = :name", Role.class)
                .setParameter("name", RoleTypes.MIN_USER).uniqueResult();

        user = new User();
        user.setLogin("userlogin");
        user.setUsername("username");
        user.setPassword("pass");
        user.setRole(role);
        user.setNonLocked(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userHibImpl.save(user, logger);

        district = new District();
        district.setName("TestDistrict");
        districtHibImpl.save(district, logger);

        shop = new Shop();
        shop.setName("TestShop");
        shop.setAddress("TestAddress");
        shop.setDistrict(district);
        shopHibImpl.save(shop, logger);

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

        userHibImpl.save(user2, logger);
        return user2;
    }


    private AvailabilitySubscription createSub(User u, Good g, Shop s) {
        AvailabilitySubscription sub = new AvailabilitySubscription();
        sub.setUser(u);
        sub.setGood(g);
        sub.setShop(s);
        sub.setCreatedAt(Instant.now());
        availabilitySubHib.save(sub, logger);
        return sub;
    }


    @Test
    @DisplayName("findAllIfEmpty")
    void findAllIfEmpty() {
        AvailabilitySubFilter filters = new AvailabilitySubFilter();
        List<AvailabilitySubscription> result = availabilitySubHib.findAll(filters);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAllIfSuccessful")
    void findAllIfSuccessful() {
        createSub(user, good, shop);
        Good good1 = createGood("good1", 2d, unit);
        createSub(user, good1, shop);

        AvailabilitySubFilter filters = new AvailabilitySubFilter();
        filters.setSortType(AvailabilitySubSortType.GOOD_ID_DESC);
        List<AvailabilitySubscription> result = availabilitySubHib.findAll(filters);

        Assertions.assertEquals(2, result.size());
        Assertions.assertNotNull(result.get(0).getUser());
        Assertions.assertNotNull(result.get(0).getGood());
        Assertions.assertNotNull(result.get(0).getShop());
        Assertions.assertEquals(good1.getId(), result.get(0).getGood().getId());
    }

    @Test
    @DisplayName("findAllFilterByShopIds")
    void findAllFilterByShopIds() {
        createSub(user, good, shop);
        Shop shop1 = createShop("shop11", "address1");
        Shop shop2 = createShop("shop2", "address2");

        createSub(user, good, shop1);
        createSub(user, good, shop2);

        AvailabilitySubFilter filters = new AvailabilitySubFilter();
        filters.setSortType(AvailabilitySubSortType.SHOP_ID_DESC);
        filters.setShopIds(List.of(shop2.getId(), shop1.getId()));

        List<AvailabilitySubscription> result = availabilitySubHib.findAll(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(shop2.getId(), result.get(0).getShop().getId());
        Assertions.assertEquals(shop1.getId(), result.get(1).getShop().getId());
    }

    @Test
    @DisplayName("findAllFilterByGoodIds")
    void findAllFilterByGoodIds() {
        createSub(user, good, shop);
        Good good1 = createGood("good1", 2d, unit);
        createSub(user, good1, shop);
        Good good2 = createGood("good2", 2d, unit);
        createSub(user, good2, shop);

        AvailabilitySubFilter filters = new AvailabilitySubFilter();
        filters.setSortType(AvailabilitySubSortType.CREATED_AT_DESC);
        filters.setGoodIds(List.of(good.getId(), good2.getId()));

        List<AvailabilitySubscription> result = availabilitySubHib.findAll(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(good2.getId(), result.get(0).getGood().getId());
        Assertions.assertEquals(good.getId(), result.get(1).getGood().getId());
    }

    @Test
    @DisplayName("findAllFilterByUserIds")
    void findAllFilterByUserIds() {
        createSub(user, good, shop);
        User user1 = createUser("user1", "userese");
        createSub(user1, good, shop);

        AvailabilitySubFilter filters = new AvailabilitySubFilter();
        filters.setSortType(AvailabilitySubSortType.ASC);
        filters.setUserIds(List.of(user1.getId()));

        List<AvailabilitySubscription> result = availabilitySubHib.findAll(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(user1.getId(), result.get(0).getUser().getId());
    }

    @Test
    @DisplayName("findAllWithPagination")
    void findAllWithPagination() {
        Good good2 = createGood("Good2", 3.0, unit);
        sessionFactory.getCurrentSession().flush();

        User user2 = createUser("lo2", "user2");
        User user3= createUser("lo3", "user3");

        createSub(user, good, shop);
        createSub(user2, good2, shop);
        createSub(user3, good2, shop);

        AvailabilitySubFilter filters = new AvailabilitySubFilter();
        filters.setSortType(AvailabilitySubSortType.USER_ID_DESC);
        filters.setPage(1);
        filters.setSize(1);

        List<AvailabilitySubscription> result = availabilitySubHib.findAll(filters);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(user2.getId(), result.get(0).getUser().getId());
    }

    @Test
    @DisplayName("findAllFilterByNonExistentShop")
    void findAllFilterByNonExistentShop() {
        createSub(user, good, shop);

        AvailabilitySubFilter filters = new AvailabilitySubFilter();
        filters.setSortType(AvailabilitySubSortType.ASC);
        filters.setShopIds(List.of(99999L));

        List<AvailabilitySubscription> result = availabilitySubHib.findAll(filters);

        Assertions.assertTrue(result.isEmpty());
    }
}
