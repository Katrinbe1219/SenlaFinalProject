package org.example.core.hibernate.documents.prices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.dto.getting.prices.PriceComparisonRequest;
import org.example.core.dto.getting.prices.PriceGetDtoForUser;
import org.example.core.dto.getting.prices.PriceGetResultForModerator;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.prices.PriceFilter;
import org.example.core.hibernate.base_settings.service_dto.CheckingPriceGoodShopExistence;
import org.example.core.hibernate.base_settings.sorting_types.PriceSortTypes;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.models.*;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.RoleTypes;
import org.example.core.services.documents.prices.data.GoodShopRecord;
import org.example.core.services.documents.prices.data.OptionForUpload;
import org.hibernate.SessionFactory;
import org.junit.Assert;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        PriceHibImpl.class,
        ShopHibImpl.class,
        UnitHibImpl.class,
        UserHibImpl.class,
        GoodHibImpl.class,
        ShopHibImpl.class,
        DistrictHibImpl.class,
})
@Transactional
public class PriceHibImplTest {

    @Autowired
    DistrictHibImpl districtHib;
    @Autowired
    ShopHibImpl shopHib;

    @Autowired
    PriceHibImpl priceHib;

    @Autowired
    UnitHibImpl unitHib;

    @Autowired
    GoodHibImpl goodHib;

    @Autowired
    SessionFactory sessionFactory;

    private static final Logger logger = LogManager.getLogger(PriceHibImplTest.class);

    private Unit unit;
    private Good good;
    private User user;
    private Shop shop;
    private Role role;
    private District district;
    @Autowired
    private UserHibImpl userHibImpl;

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
        userHibImpl.save(user, logger);

        district = new District();
        district.setName("TestDistrict");
        districtHib.save(district, logger);

        shop = new Shop();
        shop.setName("TestShop");
        shop.setAddress("TestAddress");
        shop.setDistrict(district);
        shopHib.save(shop, logger);
    }

    private Price createPrice(Good good, Shop shop, BigDecimal price, boolean active) {
        Price p = new Price();
        p.setGood(good);
        p.setShop(shop);
        p.setPrice(price);
        p.setValidFrom(Instant.now().minus(20, ChronoUnit.DAYS));
        if (!active) {
            p.setValidTo(Instant.now().minus(1, ChronoUnit.DAYS));
        }
        priceHib.save(p, logger);
        return p;
    }

    private PriceCreateDto dto(Long goodId, Long shopId, BigDecimal price) {
        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(goodId);
        dto.setShopId(shopId);
        dto.setPrice(price);
        return dto;
    }


    @Test
    @DisplayName("makeInvalidManyWithReturning")
    void makeInvalidManyWithReturning() {
        Shop shopUnwanted1 = createShop("unw1","somewhere");

        Shop shopWanted1 = createShop("u1","s1");
        Shop shopWanted2 = createShop("u2","s2");

        Good good2 = createGood("g1", 3d, unit);

        createPrice(good, shop, BigDecimal.valueOf(123), false);
        //createPrice(good, shop, BigDecimal.valueOf(345), true);

        createPrice(good, shopUnwanted1, BigDecimal.valueOf(456), true);
        createPrice(good2, shopWanted1, BigDecimal.valueOf(1234), false);
        createPrice(good2, shopWanted2, BigDecimal.valueOf(12), true);



        createPrice(good, shop, BigDecimal.valueOf(50), true);
        sessionFactory.getCurrentSession().clear();
        List<Object[]> res = priceHib.makeInvalidManyWithReturning(
                List.of(good.getId(), good2.getId(), good2.getId()),
                List.of(shop.getId(), shopWanted1.getId(), shopWanted2.getId()));

        Assertions.assertEquals(2, res.size());
        Map<GoodShopRecord, BigDecimal> oldValues = res.stream().collect(Collectors.toMap(
                d-> new GoodShopRecord((Long) d[0], (Long) d[1]),
                d -> (BigDecimal) d[2]
        ));

        Assertions.assertNotNull(oldValues.get(new GoodShopRecord(good2.getId(), shopWanted2.getId())));
        Assertions.assertNotNull(oldValues.get(new GoodShopRecord(good.getId(), shop.getId())));

    }



    @Test
    @DisplayName("saveAllSkipOptionWithoutConflicts")
    void saveAllSkipOptionWithoutConflicts() {


        List<PriceCreateDto> dtos = List.of(
                dto(good.getId(), shop.getId(), BigDecimal.valueOf(30))
        );
        priceHib.saveAll(dtos, OptionForUpload.SKIP, false);

        List<PriceGetDtoForUser> prices = priceHib.getAllForUser(null,null);
        Assertions.assertEquals(1, prices.size());
        Assertions.assertEquals(0, dtos.get(0).getPrice().compareTo(prices.get(0).getPrice()));
    }

    @Test
    @DisplayName("saveAllSkipOptionDuplicateActive")
    void saveAllSkipOptionDuplicateActive() {

        createPrice(good, shop, BigDecimal.valueOf(40), true);

        List<PriceCreateDto> dtos = List.of(
                dto(good.getId(), shop.getId(), BigDecimal.valueOf(60))
        );
        priceHib.saveAll(dtos, OptionForUpload.SKIP, false);


        List<PriceGetDtoForUser> allPrices = priceHib.getAllForUser(null, null);
        Assertions.assertEquals(1, allPrices.size());
        Assertions.assertEquals(0, BigDecimal.valueOf(40).compareTo(allPrices.get(0).getPrice()));
    }
//
//
    @Test
    @DisplayName("saveAllWithStopOptionDuplicateThrowsNotCorrectInput")
    void saveAllWithStopOptionDuplicateThrowsNotCorrectInput() {
        createPrice(good, shop, BigDecimal.valueOf(70), true);

        List<PriceCreateDto> dtos = List.of(
                dto(good.getId(), shop.getId(), BigDecimal.valueOf(80))
        );
        Exception ex = Assertions.assertThrows(NotCorrectInput.class,
                () -> priceHib.saveAll(dtos, OptionForUpload.STOP, false));
        Assertions.assertTrue(ex.getMessage().contains("Цена уже существует "));
    }
//
    @Test
    @DisplayName("saveAllIfStopOptionInsertSuccessfully")
    void saveAllIfStopOptionInsertSuccessfully() {


        List<PriceCreateDto> dtos = List.of(
                dto(good.getId(), shop.getId(), BigDecimal.valueOf(350))
        );
        priceHib.saveAll(dtos, OptionForUpload.STOP, false);

        List<PriceGetDtoForUser> prices = priceHib.getAllForUser(null, null);
        Assertions.assertEquals(1, prices.size());
    }


    @Test
    @DisplayName("saveAllUpdateIfReplaceIsSkippedConflict")
    void saveAllUpdateIfReplaceIsSkippedConflict() {

        Price old = createPrice(good, shop, BigDecimal.valueOf(800), true);

        List<PriceCreateDto> dtos = List.of(
                dto(good.getId(), shop.getId(), BigDecimal.valueOf(750))
        );

        Exception ex = Assertions.assertThrows(Exception.class, () ->
                priceHib.saveAll(dtos, OptionForUpload.REPLACE, true));


    }

    @Test
    @DisplayName("saveAllUpdateIfReplaceIsSkippedNoConflict")
    void saveAllUpdateIfReplaceIsNotSkippedConflict() {

        Price old = createPrice(good, shop, BigDecimal.valueOf(800), true);

        List<PriceCreateDto> dtos = List.of(
                dto(good.getId(), shop.getId(), BigDecimal.valueOf(750))
        );

        priceHib.saveAll(dtos, OptionForUpload.REPLACE, false);
        sessionFactory.getCurrentSession().clear();


        Price check = sessionFactory.getCurrentSession().get(Price.class, old.getId());

        Assertions.assertNotNull(check);
        Assertions.assertNotNull(check.getValidTo());

        List<PriceGetDtoForUser> allPrices = priceHib.getAllForUser(null, null);
        Assertions.assertEquals(1, allPrices.size());

    }


    @Test
    @DisplayName("findAllFullVersionSuccess")
    void findAllFullVersionSuccess() {

        createPrice(good, shop, BigDecimal.valueOf(55), true);

        List<Price> prices = priceHib.findAllFullVersion();
        Assertions.assertEquals(1, prices.size());
        Price price = prices.get(0);
        Assertions.assertNotNull(price.getGood());
        Assertions.assertNotNull(price.getShop());
        Assertions.assertEquals("TestGood", price.getGood().getName());

    }


    @Test
    @DisplayName("getByIdForModeratorIfExist")
    void getByIdForModeratorIfExist() {

        Price price = createPrice(good, shop, BigDecimal.valueOf(450), true);

        PriceGetResultForModerator result = priceHib.getByIdForModerator(price.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(good.getId(), result.getGoodId());
        Assertions.assertEquals(shop.getId(), result.getShopId());
        Assertions.assertEquals(0, price.getPrice().compareTo(result.getPrice()));
    }

    @Test
    @DisplayName("getByIdForModeratorIfDoesNotExist")
    void getByIdForModeratorIfDoesNotExist() {


        PriceGetResultForModerator result = priceHib.getByIdForModerator(999L);
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("getAllForUserReturnsOnlyActivePrices")
    void getAllForUserReturnsOnlyActivePrices() {

        createPrice(good, shop, BigDecimal.valueOf(180), false);
        Price active = createPrice(good, shop, BigDecimal.valueOf(200), true);

        List<PriceGetDtoForUser> result = priceHib.getAllForUser(null, null);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, active.getPrice().compareTo(result.get(0).getPrice()));
    }


    @Test
    @DisplayName("getAllForUserFilterByShop")
    void getAllForUserFilterByShop() {

        Shop shop2 = createShop("shop1", "a2");


        createPrice(good, shop, BigDecimal.valueOf(25), true);
        createPrice(good, shop2, BigDecimal.valueOf(30), true);

        List<PriceGetDtoForUser> result = priceHib.getAllForUser(shop.getId(), null);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("TestShop", result.get(0).getShopName());
    }


    @Test
    @DisplayName("compareByGoodAndShopIfShopIds")
    void compareByGoodAndShopIfShopIds() {

        Shop shop1 = createShop("shop", "a1");

        createPrice(good, shop, BigDecimal.valueOf(120), true);
        createPrice(good, shop1, BigDecimal.valueOf(135), true);

        PriceComparisonRequest req = new PriceComparisonRequest();
        req.setGoodId(good.getId());
        req.setShopIds(List.of(shop1.getId(), shop.getId()));

        List<PriceGetDtoForUser> result = priceHib.compareByGoodAndShop(req);
        Assertions.assertEquals(2, result.size());
    }


    @Test
    @DisplayName("checkBeforeAddPriceIfPriceDoesNotExist")
    void checkBeforeAddPriceIfPriceDoesNotExist() {

        CheckingPriceGoodShopExistence result = priceHib.checkBeforeAddPrice(shop.getId(), good.getId());
        Assertions.assertNotNull(result.getShopId());
        Assertions.assertNotNull(result.getGoodId());
        Assertions.assertNull(result.getPriceId());
        Assertions.assertNull(result.getPrice());
    }


    @Test
    @DisplayName("checkBeforeAddPriceIfPriceExist")
    void checkBeforeAddPriceIfPriceExist() {
        Price price = createPrice(good, shop, BigDecimal.valueOf(89), true);

        CheckingPriceGoodShopExistence result = priceHib.checkBeforeAddPrice(shop.getId(), good.getId());
        Assertions.assertNotNull(result.getPriceId());
        Assertions.assertEquals(0, price.getPrice().compareTo(result.getPrice()));
    }

    @Test
    @DisplayName("makeInvalidPriceIfPrice")
    void makeInvalidPriceIfPrice() {

        createPrice(good, shop, BigDecimal.valueOf(150), true);

        int updated = priceHib.makeInvalidPrice(good.getId(), shop.getId());
        Assertions.assertEquals(1, updated);
        sessionFactory.getCurrentSession().clear();

        Price p = sessionFactory.getCurrentSession().createQuery("SELECT p FROM Price p" +
                        " WHERE p.good.id = :gid AND p.shop.id = :sid " +
                        "AND p.validTo IS NOT NULL", Price.class)
                .setParameter("gid", good.getId())
                .setParameter("sid", shop.getId())
                .getSingleResult();
        Assertions.assertNotNull(p.getValidTo());
    }

    @Test
    @DisplayName("makeInvalidPriceIfNoPrice")
    void makeInvalidPriceIfNoPrice() {
        int updated = priceHib.makeInvalidPrice(good.getId(), shop.getId());
        Assertions.assertEquals(0, updated);
    }


    @Test
    @DisplayName("getPricesByFilterOnlyCurrent")
    void getPricesByFilterOnlyCurrent() {

        createPrice(good, shop, BigDecimal.valueOf(60), false);
        createPrice(good, shop, BigDecimal.valueOf(70), true);


        PriceFilter filter = new PriceFilter();
        filter.setCurrent(true);

        List<PriceGetResultForModerator> result = priceHib.getPricesByFilter(filter, null, null);
        Assertions.assertEquals(1, result.size());
        Assertions.assertNull(result.get(0).getValidTo());
    }


    @Test
    @DisplayName("getPricesByFilterPriceRange")
    void getPricesByFilterPriceRange() {

        Shop shop1 = createShop("name1", "address");
        Shop shop2 = createShop("name2", "address1");

        createPrice(good, shop, BigDecimal.valueOf(100), true);
        createPrice(good, shop1, BigDecimal.valueOf(250), true);
        createPrice(good, shop2, BigDecimal.valueOf(500), true);

        PriceFilter filter = new PriceFilter();
        filter.setMinPrice(new BigDecimal("200"));
        filter.setMaxPrice(new BigDecimal("400"));

        List<PriceGetResultForModerator> result = priceHib.getPricesByFilter(filter, null, null);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, BigDecimal.valueOf(250).compareTo(result.get(0).getPrice()));
    }

    @Test
    @DisplayName("getPricesByFilterSortPriceDesc")
    void getPricesByFilterSortPriceDesc() {
        Shop shop1 = createShop("shop1", "adres");
        createPrice(good, shop1, BigDecimal.valueOf(300), true);
        createPrice(good, shop, BigDecimal.valueOf(500), true);

        PriceFilter filter = new PriceFilter();
        filter.setSortType(PriceSortTypes.PRICE_DESC);
        List<PriceGetResultForModerator> result = priceHib.getPricesByFilter(filter, null, null);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(0, BigDecimal.valueOf(500).compareTo(result.get(0).getPrice()));
        Assertions.assertEquals(0, BigDecimal.valueOf(300).compareTo(result.get(1).getPrice()));
    }


    @Test
    @DisplayName("getPricesByFilterShopDoesNotExist")
    void getPricesByFilterShopDoesNotExist() {
        PriceFilter filter = new PriceFilter();
        filter.setShopIds(List.of(99999L));
        List<PriceGetResultForModerator> result = priceHib.getPricesByFilter(filter, null, null);
        Assertions.assertTrue(result.isEmpty());
    }
//


}
