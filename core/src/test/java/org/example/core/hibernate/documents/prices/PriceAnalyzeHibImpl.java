package org.example.core.hibernate.documents.prices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.getting.goods.GoodAnalyseForShopDto;
import org.example.core.dto.getting.goods.GoodPriceInShop;
import org.example.core.dto.getting.prices.PriceInTime;
import org.example.core.dto.getting.statistics.CartStatisticRequest;
import org.example.core.dto.getting.statistics.DistrictStatisticDto;
import org.example.core.dto.getting.statistics.shops.ShopCartDto;
import org.example.core.dto.getting.statistics.shops.ShopStatByCategoryDto;
import org.example.core.dto.getting.statistics.shops.ShopStatisticDto;
import org.example.core.hibernate.base_settings.filters.goods.GoodPriceInShopsFilter;
import org.example.core.hibernate.base_settings.filters.prices.DistrictStatisticFilter;
import org.example.core.hibernate.base_settings.filters.prices.PriceInTimeFilter;
import org.example.core.hibernate.base_settings.filters.prices.ShopStatByCategoryFilter;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.models.*;
import org.example.core.models.types.GoodStatusFromModerator;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        PriceAnalyseHibImpl.class,
        GoodHibImpl.class,
        UnitHibImpl.class,
        ShopHibImpl.class,
        DistrictHibImpl.class,
        CategoryHibImpl.class,
        PriceHibImpl.class,

})
@Transactional
class PriceAnalyseHibImplTest {

    @Autowired
    PriceAnalyseHibImpl priceAnalyseHib;
    @Autowired
    PriceHibImpl priceHib;

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    UnitHibImpl unitHib;

    @Autowired
    GoodHibImpl goodHib;

    @Autowired
    ShopHibImpl shopHib;

    @Autowired
    CategoryHibImpl categoryHib;

    private static final Logger logger = LogManager.getLogger(PriceAnalyseHibImplTest.class);

    private Unit unit;
    private Good good;
    private Shop shop;
    private District district;
    private Category category;

    private Unit createUnit() {
        Unit unit = new Unit();
        unit.setShortName("short");
        unit.setFullName("full");
        unitHib.save(unit, logger);
        return unit;
    }

    @BeforeEach
    void setUp() {
        unit = createUnit();

        district = new District();
        district.setName("TestDistrict");
        sessionFactory.getCurrentSession().persist(district);

        shop = new Shop();
        shop.setName("TestShop");
        shop.setAddress("TestAddress");
        shop.setDistrict(district);
        sessionFactory.getCurrentSession().persist(shop);

        category = new Category();
        category.setName("TestCategory");
        sessionFactory.getCurrentSession().persist(category);

        good = new Good();
        good.setName("TestGood");
        good.setUnit(unit);
        good.setCategory(category);
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        good.setRate(4.0);
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        sessionFactory.getCurrentSession().persist(good);

        sessionFactory.getCurrentSession().flush();
    }

    private Shop createShop(String name, String address) {
        Shop shop = new Shop();
        shop.setName(name);
        shop.setAddress(address);
        shop.setDistrict(district);
        shopHib.save(shop, logger);
        return shop;
    }

    private Category createCategory(String name, Category parent) {
        Category category = new Category();
        category.setName(name);
        category.setParent(parent);
        categoryHib.save(category, logger);
        return category;
    }



    private Price createActivePrice(Good good, Shop shop, BigDecimal price) {
        Price p = new Price();
        p.setGood(good);

        p.setShop(shop);
        p.setPrice(price);
        p.setValidFrom(Instant.now().minus(20, ChronoUnit.DAYS));

        priceHib.save(p, logger);
        return p;
    }

    private Price createPrice(Good g, Shop s, BigDecimal price, Instant validFrom, Instant validTo) {
        Price p = new Price();
        p.setGood(g);
        p.setShop(s);
        p.setPrice(price);
        p.setValidFrom(validFrom);
        p.setValidTo(validTo);
        priceHib.save(p, logger);
        return p;
    }

    private Good createGood(String name,  Double rate) {
        Good good = new Good();
        good.setName(name);
        good.setUnit(unit);
        good.setCategory(category);
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        good.setRate(rate);
        goodHib.save(good, logger);
        return good;
    }





    @Test
    @DisplayName("getShopStatisticIfNoPrices")
    void getShopStatisticIfNoPrices() {
        ShopStatisticDto result = priceAnalyseHib.getShopStatistic(shop.getId());
        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getMinPrice());
        Assertions.assertNull(result.getMaxPrice());
    }

    @Test

    @DisplayName("getShopStatisticIfPricesExist")
    void getShopStatisticIfPricesExist() {
        Good good2 = createGood("good1", 3d);

        createActivePrice(good, shop, BigDecimal.valueOf(100));
        createActivePrice(good2, shop, BigDecimal.valueOf(200));

        ShopStatisticDto result = priceAnalyseHib.getShopStatistic(shop.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.getMinPrice().compareTo(BigDecimal.valueOf(100)));
        Assertions.assertEquals(0, result.getMaxPrice().compareTo(BigDecimal.valueOf(200)));
    }


    @Test
    @DisplayName("getExpensiveGoodsByShopIfEmpty")
    void getExpensiveGoodsByShopIfEmpty() {
        List<GoodAnalyseForShopDto> result = priceAnalyseHib.getExpensiveGoodsByShop(shop.getId(), 5);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getExpensiveGoodsByShopSortedDescByPrice")
    void getExpensiveGoodsByShopSortedDescByPrice() {
        Good good2 = createGood("good2", 3d);

        createActivePrice(good, shop, BigDecimal.valueOf(100));
        createActivePrice(good2, shop, BigDecimal.valueOf(300));

        List<GoodAnalyseForShopDto> result = priceAnalyseHib.getExpensiveGoodsByShop(shop.getId(), 5);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(0, BigDecimal.valueOf(300).compareTo(result.get(0).getPrice()));
    }

    @Test
    @DisplayName("getExpensiveGoodsByShopLimitedByCount")
    void getExpensiveGoodsByShopLimitedByCount() {
        Good good2 = createGood("good2", 4d);

        createActivePrice(good, shop, BigDecimal.valueOf(100));
        createActivePrice(good2, shop, BigDecimal.valueOf(300));

        List<GoodAnalyseForShopDto> result = priceAnalyseHib.getExpensiveGoodsByShop(shop.getId(), 1);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(0, result.get(0).getPrice().compareTo(BigDecimal.valueOf(300)));
    }


    @Test
    @DisplayName("getCheapestGoodsByShopIfEmpty")
    void getCheapestGoodsByShopIfEmpty() {
        List<GoodAnalyseForShopDto> result = priceAnalyseHib.getCheapestGoodsByShop(shop.getId(), 5);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getCheapestGoodsByShopSortedAscByPrice")
    void getCheapestGoodsByShopSortedAscByPrice() {
        Good good2 = createGood("good2", 4d);

        createActivePrice(good, shop, BigDecimal.valueOf(100));
        createActivePrice(good2, shop, BigDecimal.valueOf(300));

        List<GoodAnalyseForShopDto> result = priceAnalyseHib.getCheapestGoodsByShop(shop.getId(), 5);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(0, BigDecimal.valueOf(100).compareTo(result.get(0).getPrice()));
    }


    @Test
    @DisplayName("getGoodPriceInTimeIfEmpty")
    void getGoodPriceInTimeIfEmpty() {
        PriceInTimeFilter filters = new PriceInTimeFilter();
        filters.setShopId(shop.getId());
        filters.setGoodId(good.getId());
        filters.setStartDate(LocalDate.now().minusDays(10));
        filters.setEndDate(LocalDate.now());

        List<PriceInTime> result = priceAnalyseHib.getGoodPriceInTime(filters);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getGoodPriceInTimeIfFound")
    void getGoodPriceInTimeIfFound() {
        createActivePrice(good, shop, BigDecimal.valueOf(100));

        PriceInTimeFilter filters = new PriceInTimeFilter();
        filters.setShopId(shop.getId());
        filters.setGoodId(good.getId());
        filters.setStartDate(LocalDate.now().minusDays(1));
        filters.setEndDate(LocalDate.now().plusDays(1));

        List<PriceInTime> result = priceAnalyseHib.getGoodPriceInTime(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(0,Double.valueOf(100).compareTo(result.get(0).getPrice()));
    }

    @Test
    @DisplayName("getGoodPriceInTimeSortedAscByValidFrom")
    void getGoodPriceInTimeSortedAscByValidFrom() {
        createPrice(good, shop, BigDecimal.valueOf(100),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(2, ChronoUnit.DAYS));

        createActivePrice(good, shop, BigDecimal.valueOf(200));

        PriceInTimeFilter filters = new PriceInTimeFilter();
        filters.setShopId(shop.getId());
        filters.setGoodId(good.getId());
        filters.setStartDate(LocalDate.now().minusDays(10));
        filters.setEndDate(LocalDate.now().plusDays(1));

        List<PriceInTime> result = priceAnalyseHib.getGoodPriceInTime(filters);

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(
                result.get(0).getValidFrom().isBefore(result.get(1).getValidFrom())
        );
    }


    @Test
    @DisplayName("getGoodPricesInShopsIfEmpty")
    void getGoodPricesInShopsIfEmpty() {
        GoodPriceInShopsFilter filters = new GoodPriceInShopsFilter();
        filters.setGoodId(good.getId());

        List<GoodPriceInShop> result = priceAnalyseHib.getGoodPricesInShops(filters);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getGoodPricesInShopsReturnsActiveOnly")
    void getGoodPricesInShopsReturnsActiveOnly() {
        createActivePrice(good, shop, BigDecimal.valueOf(100));
        createPrice(good, shop, BigDecimal.valueOf(50),
                Instant.now().minus(10, ChronoUnit.DAYS),
                Instant.now().minus(5, ChronoUnit.DAYS));

        GoodPriceInShopsFilter filters = new GoodPriceInShopsFilter();
        filters.setGoodId(good.getId());

        List<GoodPriceInShop> result = priceAnalyseHib.getGoodPricesInShops(filters);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(100.0, result.get(0).getPrice());
    }

    @Test
    @DisplayName("getGoodPricesInShopsFilterByShopIds")
    void getGoodPricesInShopsFilterByShopIds() {
        Shop shop2 = createShop("shop1", "newAd");

        createActivePrice(good, shop, BigDecimal.valueOf(100));
        createActivePrice(good, shop2, BigDecimal.valueOf(200));

        GoodPriceInShopsFilter filters = new GoodPriceInShopsFilter();
        filters.setGoodId(good.getId());
        filters.setShopIds(List.of(shop.getId()));

        List<GoodPriceInShop> result = priceAnalyseHib.getGoodPricesInShops(filters);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(shop.getId(), result.get(0).getShopId());
    }


    @Test
    @DisplayName("compareCartInShopsIfPricesExist")
    void compareCartInShopsIfPricesExist() {
        createActivePrice(good, shop, BigDecimal.valueOf(100));

        CartStatisticRequest request = new CartStatisticRequest();
        request.setGoodIds(List.of(good.getId()));
        request.setShopIds(List.of(shop.getId()));

        List<ShopCartDto> result = priceAnalyseHib.compareCartInShops(request);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(shop.getId(), result.get(0).getShopId());
        Assertions.assertEquals(0,
                result.get(0).getTotalPrice().compareTo(BigDecimal.valueOf(100)));
    }

    @Test
    @DisplayName("compareCartInShopsOutOfStockGoods")
    void compareCartInShopsOutOfStockGoods() {
        CartStatisticRequest request = new CartStatisticRequest();
        request.setGoodIds(List.of(good.getId()));
        request.setShopIds(List.of(shop.getId()));

        List<ShopCartDto> result = priceAnalyseHib.compareCartInShops(request);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertNotNull(result.get(0).getOutOfStockGoods());
        Assertions.assertTrue(result.get(0).getOutOfStockGoods().contains(good.getName()));
    }

    @Test
    @DisplayName("compareCartInShopsSortedByTotalPriceDesc")
    void compareCartInShopsSortedByTotalPriceDesc() {
        Shop shop2 = createShop("shop2", "newAD");

        createActivePrice(good, shop, BigDecimal.valueOf(100));
        createActivePrice(good, shop2, BigDecimal.valueOf(300));

        CartStatisticRequest request = new CartStatisticRequest();
        request.setGoodIds(List.of(good.getId()));
        request.setShopIds(List.of(shop.getId(), shop2.getId()));

        List<ShopCartDto> result = priceAnalyseHib.compareCartInShops(request);

        Assertions.assertEquals(2, result.size());

    }


    @Test
    @DisplayName("getShopsStatisticsBySubCategoriesIfNoPrices")
    void getShopsStatisticsBySubCategoriesIfNoPrices() {

        createCategory("sub", category);

        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();

        List<ShopStatByCategoryDto> result =
                priceAnalyseHib.getShopsStatisticsBySubCategories(filters);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(1, result.get(0).getCategories().size());
        Assertions.assertEquals(BigDecimal.ZERO, result.get(0).getCategories().get(0).getAvgPrice());
        Assertions.assertEquals(BigDecimal.ZERO, result.get(0).getCategories().get(0).getMaxPrice());
        Assertions.assertEquals(BigDecimal.ZERO, result.get(0).getCategories().get(0).getMinPrice());

    }

    @Test
    @DisplayName("getShopsStatisticsBySubCategoriesFilterByShopIds")
    void getShopsStatisticsBySubCategoriesFilterByShopIds() {

        Category sub =createCategory("sub", category);


        good.setCategory(sub);
        sessionFactory.getCurrentSession().flush();

        createActivePrice(good, shop, BigDecimal.valueOf(100));

        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(shop.getId()));

        List<ShopStatByCategoryDto> result =
                priceAnalyseHib.getShopsStatisticsBySubCategories(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(result.stream()
                .allMatch(s -> s.getShopId().equals(shop.getId())));
    }


    @Test
    @DisplayName("getShopsStatisticsByMainCategoriesIfNoPrices")
    void getShopsStatisticsByMainCategoriesIfNoPrices() {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();

        List<ShopStatByCategoryDto> result =
                priceAnalyseHib.getShopsStatisticsByMainCategories(filters);

        Assertions.assertNotNull(result);
    }

    @Test
    @DisplayName("getShopsStatisticsByMainCategoriesWithPrices")
    void getShopsStatisticsByMainCategoriesWithPrices() {
        createActivePrice(good, shop, BigDecimal.valueOf(100));

        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(shop.getId()));

        List<ShopStatByCategoryDto> result =
                priceAnalyseHib.getShopsStatisticsByMainCategories(filters);

        Assertions.assertFalse(result.isEmpty());
        result.forEach(s -> Assertions.assertFalse(s.getCategories().isEmpty()));
    }


    @Test
    @DisplayName("getDistrictStatisticIfNoPrices")
    void getDistrictStatisticIfNoPrices() {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();

        List<DistrictStatisticDto> result = priceAnalyseHib.getDistrictStatistic(filters);

        Assertions.assertNotNull(result);
    }

    @Test
    @DisplayName("getDistrictStatisticWithPrices")
    void getDistrictStatisticWithPrices() {
        createActivePrice(good, shop, BigDecimal.valueOf(100));

        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setDistrictIds(List.of(district.getId()));

        List<DistrictStatisticDto> result = priceAnalyseHib.getDistrictStatistic(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(result.stream()
                .anyMatch(d -> d.getDistrictId().equals(district.getId())));
    }

    @Test
    @DisplayName("getDistrictStatisticFilterByGoodIds")
    void getDistrictStatisticFilterByGoodIds() {
        createActivePrice(good, shop, BigDecimal.valueOf(100));

        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setGoodIds(List.of(good.getId()));

        List<DistrictStatisticDto> result = priceAnalyseHib.getDistrictStatistic(filters);

        Assertions.assertFalse(result.isEmpty());
    }
}
