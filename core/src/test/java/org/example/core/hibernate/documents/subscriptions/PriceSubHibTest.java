package org.example.core.hibernate.documents.subscriptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.hibernate.base_settings.filters.subscriptions.PriceSubFilter;
import org.example.core.hibernate.base_settings.service_dto.CheckForPriceSubscription;
import org.example.core.hibernate.base_settings.sorting_types.PriceSubSortType;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.documents.prices.PriceHibImpl;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        PriceSubHib.class,
        GoodHibImpl.class,
        UnitHibImpl.class,
        UserHibImpl.class,
        ShopHibImpl.class,
        DistrictHibImpl.class,
        PriceHibImpl.class,
})
@Transactional
class PriceSubHibTest {
    @Autowired
    UnitHibImpl unitHib;
    @Autowired
    GoodHibImpl goodHib;

    @Autowired
    PriceSubHib priceSubHib;
    @Autowired
    PriceHibImpl priceHib;
    @Autowired
    SessionFactory sessionFactory;
    @Autowired
    UserHibImpl userHib;
    @Autowired
    ShopHibImpl shopHib;

    private static final Logger logger = LogManager.getLogger(PriceSubHibTest.class);

    private Unit unit;
    private Good good;
    private User user;
    private Shop shop;
    private Role role;
    private District district;

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
        shopHib.save(shop, logger);
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
                .setParameter("name", RoleTypes.MIN_USER)
                .uniqueResult();

        user = new User();
        user.setLogin("userlogin");
        user.setUsername("username");
        user.setPassword("pass");
        user.setRole(role);
        user.setNonLocked(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        sessionFactory.getCurrentSession().persist(user);

        district = new District();
        district.setName("TestDistrict");
        sessionFactory.getCurrentSession().persist(district);

        shop = new Shop();
        shop.setName("TestShop");
        shop.setAddress("TestAddress");
        shop.setDistrict(district);
        sessionFactory.getCurrentSession().persist(shop);

        sessionFactory.getCurrentSession().flush();
    }

    private PriceSubscription createSub(User u, Good g, Shop s, BigDecimal targetPrice) {
        PriceSubscription sub = new PriceSubscription();
        sub.setUser(u);
        sub.setGood(g);
        sub.setShop(s);
        sub.setTargetPrice(targetPrice);
        sub.setCreatedAt(Instant.now());
        priceSubHib.save(sub, logger);
        return sub;
    }

    private Price createPrice(Good g, Shop s, BigDecimal price) {
        Price p = new Price();
        p.setGood(g);
        p.setShop(s);
        p.setPrice(price);
        p.setValidFrom(Instant.now());
        p.setValidTo(null);
        priceHib.save(p, logger);
        return p;
    }

    private User createUser(String login, String username, Role role) {
        User u = new User();
        u.setLogin(login);
        u.setUsername(username);
        u.setPassword("pass");
        u.setRole(role);
        u.setNonLocked(true);
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        userHib.save(u, logger);
        return u;
    }


    @Test
    @DisplayName("findAllIfEmpty")
    void findAllIfEmpty() {
        PriceSubFilter filters = new PriceSubFilter();
        List<PriceSubscription> result = priceSubHib.findAll(filters);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAllIfSuccessful")
    void findAllIfSuccessful() {
        createSub(user, good, shop, BigDecimal.valueOf(100));

        PriceSubFilter filters = new PriceSubFilter();
        List<PriceSubscription> result = priceSubHib.findAll(filters);

        Assertions.assertEquals(1, result.size());
        Assertions.assertNotNull(result.get(0).getUser());
        Assertions.assertNotNull(result.get(0).getGood());
        Assertions.assertNotNull(result.get(0).getShop());
        Assertions.assertNotNull(result.get(0).getShop().getDistrict());
    }

    @Test
    @DisplayName("findAllFilterByCurPrice")
    void findAllFilterByCurPrice() {
        PriceSubscription sub = createSub(user, good, shop, BigDecimal.valueOf(100));

        Good good2 = createGood("Good2", 3.0, unit);

        User user2 = createUser("login2", "username2", user.getRole());
        createSub(user2, good2, shop, BigDecimal.valueOf(200));

        PriceSubFilter filters = new PriceSubFilter();
        filters.setCurPrice(BigDecimal.valueOf(100));

        List<PriceSubscription> result = priceSubHib.findAll(filters);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(sub.getTargetPrice(), result.get(0).getTargetPrice());
    }

    @Test
    @DisplayName("findAllFilterByMinPrice")
    void findAllFilterByRangePrice() {
        createSub(user, good, shop, BigDecimal.valueOf(50));

        Good good2 = createGood("Good2", 3.0, unit);
        User user2 = createUser("login2", "username2", user.getRole());
        PriceSubscription first =createSub(user2, good2, shop, BigDecimal.valueOf(150));
        PriceSubscription second =createSub(user, good2, shop, BigDecimal.valueOf(120));
        createSub(user2, good, shop, BigDecimal.valueOf(230));


        PriceSubFilter filters = new PriceSubFilter();
        filters.setSortType(PriceSubSortType.CREATED_AT_DESC);
        filters.setMinPrice(BigDecimal.valueOf(100));
        filters.setMaxPrice(BigDecimal.valueOf(200));

        List<PriceSubscription> result = priceSubHib.findAll(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(second.getTargetPrice(), result.get(0).getTargetPrice());
        Assertions.assertEquals(first.getTargetPrice(), result.get(1).getTargetPrice());
    }



    @Test
    @DisplayName("findAllFilterByShopIds")
    void findAllFilterByShopIdsAndGoodIds() {
        createSub(user, good, shop, BigDecimal.valueOf(100));

        Shop shop1 = createShop("shop1", "address");

        Good good2 = createGood("Good2", 3.0, unit);
        Good good3 = createGood("Good3", 3.0, unit);

        createSub(user, good3, shop1, BigDecimal.valueOf(150));

        createSub(user, good3, shop, BigDecimal.valueOf(120));
        createSub(user, good2, shop, BigDecimal.valueOf(120));
        createSub(user, good, shop, BigDecimal.valueOf(230));

        PriceSubFilter filters = new PriceSubFilter();
        filters.setSortType(PriceSubSortType.GOOD_ID_DESC);
        filters.setShopIds(List.of(shop.getId()));
        filters.setGoodIds(List.of(good3.getId(), good2.getId()));

        List<PriceSubscription> result = priceSubHib.findAll(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(good3.getId(), result.get(0).getGood().getId());
        Assertions.assertEquals(good2.getId(), result.get(1).getGood().getId());
    }



    @Test
    @DisplayName("findAllFilterByUserIds")
    void findAllFilterByUserIds() {
        createSub(user, good, shop, BigDecimal.valueOf(100));
        User user1 = createUser("user1", "usernAmE", role);
        createSub(user1, good, shop, BigDecimal.valueOf(120));

        PriceSubFilter filters = new PriceSubFilter();
        filters.setUserIds(List.of(user.getId()));

        List<PriceSubscription> result = priceSubHib.findAll(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(user.getId(), result.get(0).getUser().getId());
    }

    @Test
    @DisplayName("findAllWithPagination")
    void findAllWithPagination() {
        Good good2 = createGood("Good2", 3.0, unit);
        User user2 = createUser("login2", "username2", user.getRole());

        createSub(user, good, shop, BigDecimal.valueOf(100));
        PriceSubscription sub = createSub(user2, good2, shop, BigDecimal.valueOf(200));

        PriceSubFilter filters = new PriceSubFilter();
        filters.setSortType(PriceSubSortType.PRICE_DESC);
        filters.setPage(0);
        filters.setSize(1);

        List<PriceSubscription> result = priceSubHib.findAll(filters);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(sub.getTargetPrice(), result.get(0).getTargetPrice());
    }

    @Test
    @DisplayName("findAllFilterByNonExistentShop")
    void findAllFilterByNonExistentShop() {
        createSub(user, good, shop, BigDecimal.valueOf(100));

        PriceSubFilter filters = new PriceSubFilter();
        filters.setSortType(PriceSubSortType.ASC);
        filters.setShopIds(List.of(99999L));

        List<PriceSubscription> result = priceSubHib.findAll(filters);

        Assertions.assertTrue(result.isEmpty());
    }



    @Test
    @DisplayName("checkingIfShopAndGoodExistNoPriceNoSub")
    void checkingIfShopAndGoodExistNoPriceNoSub() {
        CheckForPriceSubscription result = priceSubHib.checking(
                shop.getId(), good.getId(), user.getId()
        );

        Assertions.assertNotNull(result.getShopId());
        Assertions.assertNotNull(result.getGoodId());
        Assertions.assertNull(result.getPriceId());
        Assertions.assertNull(result.getPriceSubId());
    }

    @Test
    @DisplayName("checkingIfPriceExists")
    void checkingIfPriceExists() {
        Price sub = createPrice(good, shop, BigDecimal.valueOf(100.00));

        CheckForPriceSubscription result = priceSubHib.checking(
                shop.getId(), good.getId(), user.getId()
        );

        Assertions.assertNotNull(result.getPriceId());
        Assertions.assertNotNull(result.getPrice());
        Assertions.assertEquals(0, sub.getPrice().compareTo(result.getPrice()));
        Assertions.assertNull(result.getPriceSubId());
    }

    @Test
    @DisplayName("checkingIfSubExists")
    void checkingIfSubExists() {
        createSub(user, good, shop, BigDecimal.valueOf(80));

        CheckForPriceSubscription result = priceSubHib.checking(
                shop.getId(), good.getId(), user.getId()
        );

        Assertions.assertNotNull(result.getPriceSubId());
    }

    @Test
    @DisplayName("checkingIfEverythingExists")
    void checkingIfEverythingExists() {
        Price price = createPrice(good, shop, BigDecimal.valueOf(100.00));
        createSub(user, good, shop, BigDecimal.valueOf(80.00));

        CheckForPriceSubscription result = priceSubHib.checking(
                shop.getId(), good.getId(), user.getId()
        );

        Assertions.assertNotNull(result.getShopId());
        Assertions.assertNotNull(result.getGoodId());
        Assertions.assertNotNull(result.getPriceId());
        Assertions.assertNotNull(result.getPriceSubId());
        Assertions.assertEquals(0, price.getPrice().compareTo(result.getPrice()));
    }
}
