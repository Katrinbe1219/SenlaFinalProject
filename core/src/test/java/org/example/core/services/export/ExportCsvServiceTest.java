package org.example.core.services.export;

import org.example.core.dto.UnitDto;
import org.example.core.dto.export.PriceHistoryByGoodAndShop;
import org.example.core.dto.export.ShopsCurrentPricesDto;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.dto.getting.goods.GoodForReviewDto;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.dto.getting.users.ModeratorSmallDto;
import org.example.core.dto.getting.users.UserForReviewDto;
import org.example.core.hibernate.base_settings.filters.exporting.ExportShopsCurrentPricesFilter;
import org.example.core.hibernate.base_settings.service_dto.RateExportDto;
import org.example.core.models.types.RatingStatus;
import org.example.core.models.types.RatingTriggerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ExportCsvServiceTest {

    @InjectMocks
    ExportCsvService exportCsvService;

    @Test
    @DisplayName("getRecalculationsValid")
    void getRecalculationsValid(){
        RateExportDto dto = new RateExportDto();
        dto.setId(1L);
        dto.setGoodId(1L);
        dto.setCategoryId(2L);
        dto.setGoodName("g_name");
        dto.setCategoryName("c_name");
        dto.setRecalculatedAt(Instant.now());
        dto.setRate(1d);
        dto.setTriggeredBy(RatingTriggerType.SCHEDULED);
        dto.setErrorMessage("message");
        dto.setRatingStatus(RatingStatus.FAILED);

        byte[] res = exportCsvService.getRecalculations(List.of(dto));
        String content = new String(res, Charset.forName("Windows-1251"));

        Assertions.assertTrue(content.contains("id;good_id;good_name;category_id;category_name;recalculated_at;triggeredBy;rating_status;errorMessage;rate"));
        Assertions.assertTrue(content.contains("1;1;g_name;2;c_name"));
        Assertions.assertTrue(content.contains("message;1"));
        Assertions.assertTrue(content.contains("SCHEDULED;FAILED"));
    }

    @Test
    @DisplayName("getRecalculationsOnlyHeader")
    void getRecalculationsOnlyHeader(){

        byte[] res = exportCsvService.getRecalculations(List.of());
        String content = new String(res, Charset.forName("Windows-1251"));

        Assertions.assertTrue(content.contains("id;good_id;good_name;category_id;category_name;recalculated_at;triggeredBy;rating_status;errorMessage;rate"));
        Assertions.assertEquals(1, content.lines().filter(l -> !l.isBlank()).count());
    }


    @Test
    @DisplayName("getAllGoodsValid")
    void getAllGoodsValid(){
        GoodGetFullDto dto = new GoodGetFullDto();
        CategoryGetDto cat = new CategoryGetDto();
        UnitDto unit = new UnitDto();

        dto.setId(1L);
        dto.setName("apple");

        cat.setName("fruit");
        cat.setId(2L);
        dto.setCategory(cat);

        unit.setId(1L);
        unit.setShortName("kg");
        unit.setFullName("kkgg");
        dto.setUnit(unit);



        byte[] res = exportCsvService.getAllGoods(List.of(dto));
        String content = new String(res, Charset.forName("Windows-1251"));

        Assertions.assertTrue(content.contains("id;good_name;category_id;category_name;category_parent_id;category_parent_name;unit_id;unit_short_name;unit_full_name;description;tags;updated_at;created_at"));
        Assertions.assertTrue(content.contains("1;apple;2;fruit"));
        Assertions.assertTrue(content.contains("1;kg;kkgg"));
    }

    @Test
    @DisplayName("getAllGoodsIfOnlyHeader")
    void getAllGoodsIfOnlyHeader(){

        byte[] res = exportCsvService.getAllGoods(List.of());
        String content = new String(res, Charset.forName("Windows-1251"));

        Assertions.assertTrue(content.contains("id;good_name;category_id;category_name;category_parent_id;category_parent_name;unit_id;unit_short_name;unit_full_name;description;tags;updated_at;created_at"));
        Assertions.assertFalse(content.contains("1;apple;2;fruit"));
        Assertions.assertEquals(1, content.lines().filter(l -> !l.isBlank()).count());
    }

    @Test
    @DisplayName("getReviewsHistoryValid")
    void getReviewsHistoryValid(){
        ReviewFullDto dto = new ReviewFullDto();
        GoodForReviewDto g = new GoodForReviewDto();
        UserForReviewDto u = new UserForReviewDto();
        dto.setId(1L);

        g.setId(1L);
        g.setName("apple");
        g.setAverageRate(1.5);
        dto.setGood(g);

        u.setId(1L);
        u.setBlocked(false);
        u.setRole("ADMIN");
        u.setUsername("a12");
        dto.setUser(u);



        byte[] res = exportCsvService.getReviewsHistory(List.of(dto));
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains("id;good_id;good_name;user_id;user_username;review;rate;createdAt;blocked;blockedAt;blocked_by_id;blocked_by_username"));
        Assertions.assertTrue(content.contains("1;1;apple;1;a12"));
        Assertions.assertTrue(content.contains("a12;null;null"));
    }

    @Test
    @DisplayName("getReviewsHistoryIfOnlyHeader")
    void getReviewsHistoryIfOnlyHeader(){

        byte[] res = exportCsvService.getReviewsHistory(List.of());
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains("id;good_id;good_name;user_id;user_username;review;rate;createdAt;blocked;blockedAt;blocked_by_id;blocked_by_username"));
        Assertions.assertFalse(content.contains("1;1;apple;1;a12"));
        Assertions.assertEquals(1, content.lines().filter(l -> !l.isBlank()).count());
    }

    @Test
    @DisplayName("getReviewsHistoryByGoodIdValid")
    void getReviewsHistoryByGoodIdValid(){
        ReviewFullDto dto = new ReviewFullDto();
        GoodForReviewDto g = new GoodForReviewDto();
        UserForReviewDto u = new UserForReviewDto();
        dto.setId(1L);
        dto.setRate(1);
        dto.setReview("excellent");

        u.setId(1L);
        u.setBlocked(false);
        u.setRole("ADMIN");
        u.setUsername("a12");
        dto.setUser(u);



        byte[] res = exportCsvService.getReviewsHistoryByGoodId(List.of(dto));
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains("id;user_id;user_username;review;rate;createdAt;blocked;blockedAt;blocked_by_id;blocked_by_username"));
        Assertions.assertTrue(content.contains("1;1;a12;excellent;1;"));

    }

    @Test
    @DisplayName("getReviewsHistoryByGoodIdIfOnlyHeader")
    void getReviewsHistoryByGoodIdIfOnlyHeader(){

        byte[] res = exportCsvService.getReviewsHistoryByGoodId(List.of());
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains("id;user_id;user_username;review;rate;createdAt;blocked;blockedAt;blocked_by_id;blocked_by_username"));
        Assertions.assertEquals(1, content.lines().filter(l -> !l.isBlank()).count());
    }

    @Test
    @DisplayName("getPriceHistoryByGoodIdValid")
    void getPriceHistoryByGoodIdValid(){
        PriceHistoryByGoodAndShop dto = new PriceHistoryByGoodAndShop();
        dto.setPrice(BigDecimal.valueOf(12));
        dto.setPriceId(1L);
        dto.setValidFrom(Instant.now());


        byte[] res = exportCsvService.getPriceHistoryByGoodId(List.of(dto));
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains("id;price;validFrom;validTo"));
        Assertions.assertTrue(content.contains("1;12"));
    }

    @Test
    @DisplayName("getPriceHistoryByGoodIdIfOnlyHeader")
    void getPriceHistoryByGoodIdIfOnlyHeader(){
        byte[] res = exportCsvService.getPriceHistoryByGoodId(List.of());
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains("id;price;validFrom;validTo"));
        Assertions.assertEquals(1, content.lines().filter(l -> !l.isBlank()).count());
    }

    @Test
    @DisplayName("getModeratorRecalcValid")
    void getModeratorRecalcValid(){
        ModeratorRecalcDto dto = new ModeratorRecalcDto();
        ModeratorSmallDto mod = new ModeratorSmallDto();
        GoodIdDto g = new GoodIdDto();

        dto.setId(1L);
        dto.setComment("comment");

        mod.setId(2L);
        mod.setUsername("m123");
        dto.setModerator(mod);

        g.setId(1L);
        g.setName("apple");
        dto.setGood(g);

        byte[] res = exportCsvService.getModeratorRecalc(List.of(dto));
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains("recalculation_id;moderator_id;moderator_username;good_id;good_name;comment;checked_at"));
        Assertions.assertTrue(content.contains("1;2;m123;1;apple"));

    }

    @Test
    @DisplayName("getModeratorRecalcIfOnlyHeader")
    void getModeratorRecalcIfOnlyHeader(){
        byte[] res = exportCsvService.getModeratorRecalc(List.of());
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains("recalculation_id;moderator_id;moderator_username;good_id;good_name;comment;checked_at"));
        Assertions.assertEquals(1, content.lines().filter(l -> !l.isBlank()).count());
    }

    @Test
    @DisplayName("getShopsCurrentPricesWithoutExtra")
    void getShopsCurrentPricesWithoutExtra(){
        ShopsCurrentPricesDto dto = new ShopsCurrentPricesDto();
        dto.setPrice(BigDecimal.valueOf(12));
        dto.setGoodId(1l);
        dto.setGoodName("apple");
        dto.setShopId(2l);

        List<String> headers = new ArrayList<>();
        headers.add("good_id");
        headers.add("good_name");
        headers.add("price");
        headers.add("shop_id");


        byte[] res = exportCsvService.getShopsCurrentPrices(List.of(dto), new ExportShopsCurrentPricesFilter());
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains(String.join(";", headers)));
        Assertions.assertFalse(content.contains("shop_name;address"));
        Assertions.assertFalse(content.contains("district_id;district_name"));
        Assertions.assertFalse(content.contains("category_id"));
        Assertions.assertFalse(content.contains("tags"));
        Assertions.assertTrue(content.contains("1;apple;12"));

    }

    @Test
    @DisplayName("getShopsCurrentPricesWithShops")
    void getShopsCurrentPricesWithShops(){
        ShopsCurrentPricesDto dto = new ShopsCurrentPricesDto();
        dto.setPrice(BigDecimal.valueOf(12));
        dto.setGoodId(1l);
        dto.setGoodName("apple");
        dto.setShopId(2l);

        List<String> headers = new ArrayList<>();
        headers.add("good_id");
        headers.add("good_name");
        headers.add("price");
        headers.add("shop_id");
        ExportShopsCurrentPricesFilter filters = new ExportShopsCurrentPricesFilter();
        filters.setShops(true);


        byte[] res = exportCsvService.getShopsCurrentPrices(List.of(dto), filters);
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains(String.join(";", headers)));
        Assertions.assertTrue(content.contains("shop_name;address"));
        Assertions.assertTrue(content.contains("district_id;district_name"));
        Assertions.assertFalse(content.contains("category_id"));
        Assertions.assertFalse(content.contains("tags"));
        Assertions.assertTrue(content.contains("1;apple;12"));

    }

    @Test
    @DisplayName("getShopsCurrentPricesAllExtra")
    void getShopsCurrentPricesAllExtra(){
        ShopsCurrentPricesDto dto = new ShopsCurrentPricesDto();
        dto.setPrice(BigDecimal.valueOf(12));
        dto.setGoodId(1l);
        dto.setGoodName("apple");
        dto.setShopId(2l);
        dto.setTags("tag1,tag2");
        dto.setCategoryId(5L);
        dto.setCategoryName("fruit");

        List<String> headers = new ArrayList<>();
        headers.add("good_id");
        headers.add("good_name");
        headers.add("price");
        headers.add("shop_id");
        ExportShopsCurrentPricesFilter filters = new ExportShopsCurrentPricesFilter();
        filters.setShops(true);
        filters.setTags(true);
        filters.setCategories(true);


        byte[] res = exportCsvService.getShopsCurrentPrices(List.of(dto), filters);
        String content = new String(res, Charset.forName("Windows-1251"));
        Assertions.assertTrue(content.contains(String.join(";", headers)));
        Assertions.assertTrue(content.contains("shop_name;address"));
        Assertions.assertTrue(content.contains("district_id;district_name"));
        Assertions.assertTrue(content.contains("category_id"));
        Assertions.assertTrue(content.contains("tags"));
        Assertions.assertTrue(content.contains("1;apple;12"));
        Assertions.assertTrue(content.contains("null;tag1,tag2"));

    }
}
