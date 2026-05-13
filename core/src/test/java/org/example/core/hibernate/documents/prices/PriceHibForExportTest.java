package org.example.core.hibernate.documents.prices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.export.PriceHistoryByGoodAndShop;
import org.example.core.dto.export.ShopsCurrentPricesDto;
import org.example.core.hibernate.base_settings.filters.exporting.ExportShopsCurrentPricesFilter;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.hibernate.dictionaries.TagHibImpl;
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
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        PriceHibForExport.class,
        ShopHibImpl.class,
        GoodHibImpl.class,
        DistrictHibImpl.class,
        CategoryHibImpl.class,
        TagHibImpl.class,
        UnitHibImpl.class
})
@Transactional
public class PriceHibForExportTest {

    @Autowired
    private PriceHibForExport priceHibForExport;

    @Autowired
    private ShopHibImpl shopHib;

    @Autowired
    private GoodHibImpl goodHib;

    @Autowired
    private DistrictHibImpl districtHib;

    @Autowired
    private CategoryHibImpl categoryHib;

    @Autowired
    private TagHibImpl tagHib;

    @Autowired
    private UnitHibImpl unitHib;

    @Autowired
    private SessionFactory sessionFactory;

    private static final Logger logger = LogManager.getLogger(PriceHibForExportTest.class);

    private Unit unit;
    private Good good1, good2;
    private Shop shop1, shop2;
    private District district;
    private Category category, childCategory;
    private Tag tag1, tag2;

    // ---------- вспомогательные методы ----------

    private Unit createUnit() {
        Unit unit = new Unit();
        unit.setShortName("short");
        unit.setFullName("full");
        unitHib.save(unit, logger);
        return unit;
    }

    private Good createGood(String name, Category category, List<Tag> tags) {
        Good good = new Good();
        good.setName(name);
        good.setUnit(unit);
        good.setCategory(category);
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        good.setRate(1.0);
        if (tags != null) {
            good.setTags(tags);
        }
        goodHib.save(good, logger);
        return good;
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

    private Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tagHib.save(tag, logger);
        return tag;
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
        sessionFactory.getCurrentSession().persist(p);
        sessionFactory.getCurrentSession().flush();
        return p;
    }

    @BeforeEach
    void setUp() {
        unit = createUnit();
        district = new District();
        district.setName("d1");
        districtHib.save(district, logger);

        category = createCategory("products", null);
        childCategory = createCategory("dairy", category);
        tag1 = createTag("Скидка");
        tag2 = createTag("Новинка");

        good1 = createGood("good1", childCategory, List.of(tag1));
        good2 = createGood("good2", category, List.of(tag2));

        shop1 = createShop("shopTest1", "ad1");
        shop2 = createShop("shopTest2", "ad2");
    }

    // ---------- getShopsCurrentPrices ----------

    @Test
    @DisplayName("getShopsCurrentPrices без флагов, базовые поля")
    void getShopsCurrentPricesBaseFields() {
        createPrice(good1, shop1, BigDecimal.valueOf(100), true);
        createPrice(good2, shop2, BigDecimal.valueOf(200), false);

        ExportShopsCurrentPricesFilter filter = new ExportShopsCurrentPricesFilter();


        List<ShopsCurrentPricesDto> result = priceHibForExport.getShopsCurrentPrices(filter);
        Assertions.assertEquals(1, result.size());

        ShopsCurrentPricesDto dto = result.get(0);
        Assertions.assertNotNull(dto.getGoodId());
        Assertions.assertNotNull(dto.getGoodName());
        Assertions.assertNotNull(dto.getShopId());
        Assertions.assertEquals(0, dto.getPrice().compareTo(BigDecimal.valueOf(100)));
        Assertions.assertNull(dto.getCategoryId());
        Assertions.assertNull(dto.getShopName());
        Assertions.assertNull(dto.getTags());
    }

    @Test
    @DisplayName("getShopsCurrentPricesByShopIds")
    void getShopsCurrentPricesByShopIds() {
        createPrice(good1, shop1, BigDecimal.valueOf(50), true);
        createPrice(good2, shop2, BigDecimal.valueOf(70), true);

        ExportShopsCurrentPricesFilter filter = new ExportShopsCurrentPricesFilter();
        filter.setShopsIds(List.of(shop1.getId()));

        List<ShopsCurrentPricesDto> result = priceHibForExport.getShopsCurrentPrices(filter);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(shop1.getId(), result.get(0).getShopId());
    }


    @Test
    @DisplayName("getShopsCurrentPrices с categories=true, проверка category_name и parent_id")
    void getShopsCurrentPricesWithCategoriesFlag() {
        createPrice(good1, shop1, BigDecimal.valueOf(130), true);

        ExportShopsCurrentPricesFilter filter = new ExportShopsCurrentPricesFilter();
        filter.setCategories(true);

        List<ShopsCurrentPricesDto> result = priceHibForExport.getShopsCurrentPrices(filter);
        Assertions.assertEquals(1, result.size());
        ShopsCurrentPricesDto dto = result.get(0);
        Assertions.assertEquals(childCategory.getId(), dto.getCategoryId());
        Assertions.assertEquals("dairy", dto.getCategoryName());
        Assertions.assertEquals(category.getId(), dto.getCategoryParentId());
        Assertions.assertNull(dto.getShopName());
    }

    @Test
    @DisplayName("getShopsCurrentPricesWithTagsFlag")
    void getShopsCurrentPricesWithTagsFlag() {
        createPrice(good1, shop1, BigDecimal.valueOf(90), true);
        goodHib.save(good1, logger);

        ExportShopsCurrentPricesFilter filter = new ExportShopsCurrentPricesFilter();
        filter.setTags(true);

        List<ShopsCurrentPricesDto> result = priceHibForExport.getShopsCurrentPrices(filter);
        Assertions.assertEquals(1, result.size());
        String tags = result.get(0).getTags();
        Assertions.assertNotNull(tags);
        Assertions.assertTrue(tags.contains("Скидка"));
    }

    @Test
    @DisplayName("getShopsCurrentPricesAllFlagsTrue")
    void getShopsCurrentPricesAllFlagsTrue() {
        createPrice(good1, shop1, BigDecimal.valueOf(300), true);

        ExportShopsCurrentPricesFilter filter = new ExportShopsCurrentPricesFilter();
        filter.setShops(true);
        filter.setCategories(true);
        filter.setTags(true);

        List<ShopsCurrentPricesDto> result = priceHibForExport.getShopsCurrentPrices(filter);
        Assertions.assertEquals(1, result.size());
        ShopsCurrentPricesDto dto = result.get(0);


        Assertions.assertEquals(good1.getId(), dto.getGoodId());
        Assertions.assertEquals(shop1.getId(), dto.getShopId());
        Assertions.assertEquals("shopTest1", dto.getShopName());
        Assertions.assertEquals("dairy", dto.getCategoryName());
        Assertions.assertNotNull(dto.getTags());
    }

    @Test
    @DisplayName("getShopsCurrentPricesEmptyResult")
    void getShopsCurrentPricesEmptyResult() {
        ExportShopsCurrentPricesFilter filter = new ExportShopsCurrentPricesFilter();
        List<ShopsCurrentPricesDto> result = priceHibForExport.getShopsCurrentPrices(filter);
        Assertions.assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("getPriceHistoryByGoodIdAllPrices")
    void getPriceHistoryByGoodIdAllPrices() {
        Price p1 = createPrice(good1, shop1, BigDecimal.valueOf(100), true);
        Price p2 = createPrice(good1, shop1, BigDecimal.valueOf(120), false);
        Price p3 = createPrice(good2, shop2, BigDecimal.valueOf(200), true);

        List<PriceHistoryByGoodAndShop> result = priceHibForExport.getPriceHistoryByGoodId(good1.getId(), shop1.getId());
        Assertions.assertEquals(2, result.size());

        List<Long> prices = result.stream().map(PriceHistoryByGoodAndShop::getPriceId).toList();
        Assertions.assertTrue(prices.contains(p1.getId()));
        Assertions.assertTrue(prices.contains(p2.getId()));
    }

    @Test
    @DisplayName("getPriceHistoryByGoodIdIfGoodNotExist")
    void getPriceHistoryByGoodIdIfGoodNotExist() {
        List<PriceHistoryByGoodAndShop> result = priceHibForExport.getPriceHistoryByGoodId(9999L, shop1.getId());
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getPriceHistoryByGoodIdIfShopNotExist")
    void getPriceHistoryByGoodIdIfShopNotExist() {
        List<PriceHistoryByGoodAndShop> result = priceHibForExport.getPriceHistoryByGoodId(good1.getId(), 9999L);
        Assertions.assertTrue(result.isEmpty());
    }
}
