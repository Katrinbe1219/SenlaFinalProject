package org.example.core.services.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.core.dto.DistrictDto;
import org.example.core.dto.TagDto;
import org.example.core.dto.UnitDto;
import org.example.core.dto.export.ModeratorDto;
import org.example.core.dto.export.ShopsCurrentPricesDto;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.dto.getting.goods.GoodForReviewDto;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.dto.getting.statistics.shops.ShopGetDto;
import org.example.core.dto.getting.users.ModeratorSmallDto;
import org.example.core.dto.getting.users.UserForReviewDto;
import org.example.core.models.types.ModeratorVerdict;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class ExportXlsxServiceTest {

    @InjectMocks
    ExportXlsxService service;

    private ShopsCurrentPricesDto createShopCurrentPricesDto(int choice){
        ShopsCurrentPricesDto dto = new ShopsCurrentPricesDto();
        if (choice == 1){
            dto.setPrice(BigDecimal.valueOf(111));
            dto.setShopId(1l);
            dto.setDistrictId(121l);
            dto.setDistrictName("vorsh1");
            dto.setGoodName("apple1");
            dto.setGoodId(11l);
            dto.setCategoryName("cat_name1");
            dto.setCategoryId(111l);
        }else{
            dto.setPrice(BigDecimal.valueOf(222l));
            dto.setShopId(2l);
            dto.setDistrictId(212l);
            dto.setDistrictName("vorsh2");
            dto.setGoodName("apple2");
            dto.setGoodId(22l);
            dto.setCategoryName("cat_name2");
            dto.setCategoryId(222l);
        }

        return dto;
    }

    private TagDto createTagDto(int choice){
        TagDto dto = new TagDto();
        if (choice == 1){
            dto.setId(1l);
            dto.setName("fat1");
        }else{
            dto.setId(2l);
            dto.setName("fat2");
        }

        return dto;
    }

    private ShopGetDto createShopGetDto(int choice){
        ShopGetDto dto = new ShopGetDto();
        if (choice == 1){
            DistrictDto d = new DistrictDto();
            d.setName("borh1");
            d.setId(111l);

            dto.setId(1l);
            dto.setDistrict(d);
            dto.setName("perek1");
        } else{
            DistrictDto d = new DistrictDto();
            d.setName("borh2");
            d.setId(222l);

            dto.setId(2l);
            dto.setDistrict(d);
            dto.setName("perek2");

        }        return dto;
    }



    @Test
    @DisplayName("generateReportForCurrentShopsPricesWithNoExtra")
    void generateReportForCurrentShopsPricesWithNoExtra() throws IOException {
        byte[] res = service.generateReportForCurrentShopsPrices(
                null,
                List.of(createShopCurrentPricesDto(1), createShopCurrentPricesDto(2)),
                null,
                null
        );

        XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(res));
        Assertions.assertNotNull( wb.getSheet("Prices"));

        Sheet sheet  = wb.getSheet("Prices");
        Row header = sheet.getRow(0);
        Assertions.assertEquals("Good_id", header.getCell(0).getStringCellValue());
        Assertions.assertEquals("Good_name", header.getCell(1).getStringCellValue());
        Assertions.assertEquals("Price", header.getCell(2).getStringCellValue());
        Assertions.assertEquals("ShopId", header.getCell(3).getStringCellValue());
        Assertions.assertNull(header.getCell(4)); // дальше ничего нет
        Assertions.assertNull(wb.getSheet("Shops"));
        Assertions.assertNull(wb.getSheet("Categories"));
        Assertions.assertNull(wb.getSheet("Shops"));

        Row r1 = sheet.getRow(1);
        Row r2 = sheet.getRow(2);

        Assertions.assertEquals("apple1", r1.getCell(1).getStringCellValue());
        Assertions.assertEquals("apple2", r2.getCell(1).getStringCellValue());
        wb.close();

    }

    @Test
    @DisplayName("generateReportForCurrentShopsPricesWithAllExtra")
    void generateReportForCurrentShopsPricesWithAllExtra() throws IOException {
        byte[] res = service.generateReportForCurrentShopsPrices(
                List.of(createTagDto(1)),
                List.of(createShopCurrentPricesDto(1), createShopCurrentPricesDto(2)),
                List.of(createShopGetDto(1)), List.of(createCategoryGetDto(1))
        );

        XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(res));
        Assertions.assertNotNull( wb.getSheet("Prices"));

        Sheet sheet  = wb.getSheet("Prices");
        Row header = sheet.getRow(0);
        Assertions.assertNotNull(wb.getSheet("Shops"));
        Assertions.assertNotNull(wb.getSheet("Categories"));
        Assertions.assertNotNull(wb.getSheet("Shops"));

        Assertions.assertEquals("Good_id", header.getCell(0).getStringCellValue());
        Assertions.assertEquals("Good_name", header.getCell(1).getStringCellValue());
        Assertions.assertEquals("Price", header.getCell(2).getStringCellValue());
        Assertions.assertEquals("ShopId", header.getCell(3).getStringCellValue());
        Assertions.assertEquals("Shop_name", header.getCell(4).getStringCellValue());
        Assertions.assertEquals("Shop_address", header.getCell(5).getStringCellValue());
        Assertions.assertEquals("Category_Name", header.getCell(9).getStringCellValue());

        Row r1 = sheet.getRow(1);
        Row r2 = sheet.getRow(2);
        Assertions.assertEquals("apple1", r1.getCell(1).getStringCellValue());
        Assertions.assertEquals("apple2", r2.getCell(1).getStringCellValue());

        Assertions.assertEquals(String.format("VLOOKUP(%s%d,Shops!$A:$E,2,False)", getColumnLetter(3),2),
                r1.getCell(4).getCellFormula());
        Assertions.assertEquals(String.format("VLOOKUP(%s%d,Shops!$A:$E,5,False)", getColumnLetter(3),2),
                r1.getCell(7).getCellFormula());

                wb.close();

    }

    @Test
    @DisplayName("generateReportForCurrentShopsPricesWithOnlyCategory")
    void generateReportForCurrentShopsPricesWithOnlyCategory() throws IOException {
        byte[] res = service.generateReportForCurrentShopsPrices(
                null, List.of(createShopCurrentPricesDto(1)),
               null, List.of(createCategoryGetDto(1), createCategoryGetDto(2))
        );

        XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(res));
        Assertions.assertNotNull( wb.getSheet("Prices"));

        Sheet sheet  = wb.getSheet("Prices");
        Row header = sheet.getRow(0);
        Assertions.assertNull(wb.getSheet("Shops"));
        Assertions.assertNotNull(wb.getSheet("Categories"));
        Assertions.assertNull(wb.getSheet("Shops"));

        Assertions.assertEquals("Good_id", header.getCell(0).getStringCellValue());
        Assertions.assertEquals("Good_name", header.getCell(1).getStringCellValue());
        Assertions.assertEquals("Price", header.getCell(2).getStringCellValue());
        Assertions.assertEquals("Category_Name", header.getCell(5).getStringCellValue());

        Sheet c = wb.getSheet("Categories");
        Cell c1 = c.getRow(1).getCell(2);
        Cell c2 = c.getRow(1).getCell(3);
        Assertions.assertTrue( c1 == null || c1.getCellType() == CellType.BLANK);
        Assertions.assertTrue( c2 == null || c2.getCellType() == CellType.BLANK);

        Assertions.assertEquals(1d, c.getRow(2).getCell(2).getNumericCellValue());
        Assertions.assertEquals("cat1", c.getRow(2).getCell(3).getStringCellValue());

        Row prices = sheet.getRow(1);
        Assertions.assertEquals(String.format("VLOOKUP(%s%d,Categories!$A:$D,2,False)", getColumnLetter(4),2), prices.getCell(5).getCellFormula());
        Assertions.assertEquals(111l, prices.getCell(2).getNumericCellValue());




        wb.close();
    }

    private ReviewFullDto createReviewFullDto(int choice) {
        ReviewFullDto dto = new ReviewFullDto();
        GoodForReviewDto g = new GoodForReviewDto();
        UserForReviewDto u = new UserForReviewDto();
        if (choice == 1){
            g.setId(1l);
            g.setName("good1");
            g.setAverageRate(1.1);

            u.setId(1l);
            u.setUsername("user1");
            u.setRole("ad1");
            u.setBlocked(false);

            dto.setId(1l);
            dto.setGood(g);
            dto.setReview("excellent1");
            dto.setRate(1);
            dto.setUser(u);
            dto.setBlocked(false);

        }
        else{
            UserForReviewDto m = new UserForReviewDto();

            m.setId(3l);
            m.setBlocked(false);
            m.setUsername("m1");
            m.setRole("moderator");

            dto.setBlocked(true);

            g.setId(2l);
            g.setName("good2");
            g.setAverageRate(2.2);

            u.setId(2l);
            u.setUsername("user2");
            u.setRole("ad2");
            u.setBlocked(false);

            dto.setId(2l);
            dto.setGood(g);
            dto.setReview("excellent2");
            dto.setRate(2);
            dto.setUser(u);
            dto.setBlockedAt(Instant.now());
            dto.setBlockedBy(m);
        }
        return dto;
    }


    @Test
    @DisplayName("getReviewsHistoryWithNoExtra")
    void getReviewsHistoryWithNoExtra() throws IOException {
        byte[] res = service.getReviewsHistory(
                List.of(
                        createReviewFullDto(1),
                        createReviewFullDto(2)
                ), null, null
        );
        XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(res));
        Assertions.assertNotNull( wb.getSheet("Reviews"));
        Assertions.assertNull( wb.getSheet("Goods"));
        Assertions.assertNull( wb.getSheet("Moderators"));

        Sheet sheet  = wb.getSheet("Reviews");
        Row header = sheet.getRow(0);
        Row r1 = sheet.getRow(1);
        Row r2 = sheet.getRow(2);

        Assertions.assertEquals("review", header.getCell(5).getStringCellValue());
        Assertions.assertEquals("blockedAt", header.getCell(9).getStringCellValue());

        Assertions.assertNotEquals(CellType.FORMULA, r1.getCell(4).getCellType());
        Assertions.assertNotEquals(CellType.FORMULA, r2.getCell(2).getCellType());

        Assertions.assertNotNull( r2.getCell(9).getDateCellValue());
        Assertions.assertNull( r1.getCell(9).getDateCellValue());

    }

    private GoodForReviewDto createGoodForReviewDto(int choice) {
        GoodForReviewDto dto = new GoodForReviewDto();
        if (choice == 1){
            dto.setId(1l);
            dto.setName("good1");
            dto.setAverageRate(1.1);
        }
        else{
            dto.setId(2l);
            dto.setName("good2");
            dto.setAverageRate(2.2);
        }
        return dto;
    }

    private ModeratorDto createModeratorDto(int choice) {
        ModeratorDto dto = new ModeratorDto();
        if (choice == 1){
            dto.setId(3l);
            dto.setUsername("m1");
        }else{
            dto.setId(4l);
            dto.setUsername("m2");

        }

        return dto;
    }

    @Test
    @DisplayName("getReviewsHistoryWithAllExtra")
    void getReviewsHistoryWithAllExtra() throws IOException {
        byte[] res = service.getReviewsHistory(
                List.of(
                        createReviewFullDto(1),
                        createReviewFullDto(2)
                ), Map.of(1l, createModeratorDto(1), 2l, createModeratorDto(2)),
                Map.of(1l,createGoodForReviewDto(1),
                        2l, createGoodForReviewDto(2))
        );
        XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(res));
        Assertions.assertNotNull( wb.getSheet("Reviews"));
        Assertions.assertNotNull( wb.getSheet("Goods"));
        Assertions.assertNotNull( wb.getSheet("Moderators"));

        Sheet sheet  = wb.getSheet("Reviews");
        Row header = sheet.getRow(0);
        Row r1 = sheet.getRow(1);
        Row r2 = sheet.getRow(2);

        Assertions.assertEquals("blockedBy_username", header.getCell(11).getStringCellValue());
        Assertions.assertEquals("createdAt", header.getCell(7).getStringCellValue());

        Assertions.assertEquals(CellType.FORMULA, r1.getCell(2).getCellType());
        Assertions.assertEquals(CellType.FORMULA, r2.getCell(2).getCellType());
        Assertions.assertNull(r1.getCell(10));
        Assertions.assertEquals(String.format("VLOOKUP(%s%d,Moderators!$A:$D,2,False)", getColumnLetter(10),3),
                r2.getCell(11).getCellFormula());


        Assertions.assertNotNull( r2.getCell(9).getDateCellValue());
        Assertions.assertNull( r1.getCell(9).getDateCellValue());

        Sheet goods = wb.getSheet("Goods");
        Row rg1 = goods.getRow(1);
        Row rg2 = goods.getRow(2);

        Assertions.assertEquals("good1", rg1.getCell(1).getStringCellValue());
        Assertions.assertEquals("good2", rg2.getCell(1).getStringCellValue());

        Sheet mods = wb.getSheet("Moderators");
        Row rm1 = mods.getRow(1);
        Assertions.assertEquals("m1", rm1.getCell(1).getStringCellValue());

    }

    private ModeratorSmallDto createModeratorSmallDto(int choice) {
        ModeratorSmallDto dto = new ModeratorSmallDto();
        if (choice == 1){
            dto.setId(1l);
            dto.setUsername("m1");
        }else{
            dto.setId(2l);
            dto.setUsername("m2");
        }
        return dto;
    }

    private GoodIdDto createGoodIdDto(int choice) {
        GoodIdDto dto = new GoodIdDto();
        if (choice == 1){
            dto.setName("good1");
            dto.setId(1l);
        }else{
            dto.setId(2l);
            dto.setName("good2");
        }
        return dto;
    }

    private ModeratorRecalcDto createModeratorRecalcDto(int choice) {
        ModeratorRecalcDto dto = new ModeratorRecalcDto();
        if (choice == 1){
            dto.setId(1l);
            dto.setModerator(createModeratorSmallDto(1));
            dto.setGood(createGoodIdDto(1));
            dto.setVerdict(ModeratorVerdict.SUSPICIOUS);
            dto.setComment("ex1");
            dto.setCheckAt(Instant.now());

        }else{
            dto.setId(2l);
            dto.setModerator(createModeratorSmallDto(2));
            dto.setGood(createGoodIdDto(2));
            dto.setVerdict(ModeratorVerdict.APPROVED);
            dto.setComment("ex2");
            dto.setCheckAt(Instant.now());
        }
        return dto;
    }

    @Test
    @DisplayName("getModeratorRecalcHistoryWithNoExtra")
    void getModeratorRecalcHistoryWithNoExtra() throws IOException {
        byte[] res = service.getModeratorRecalcHistory(
                List.of(createModeratorRecalcDto(1), createModeratorRecalcDto(2)),
                null,
                null);
        XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(res));

        Assertions.assertNotNull( wb.getSheet("Moderators` Recalculation"));
        Assertions.assertNull(wb.getSheet("Moderators"));
        Assertions.assertNull(wb.getSheet("Goods"));

        Sheet sheet  = wb.getSheet("Moderators` Recalculation");
        Row header = sheet.getRow(0);
        Assertions.assertEquals("good_id", header.getCell(3).getStringCellValue());
        Assertions.assertEquals("comment", header.getCell(6).getStringCellValue());

        Row r1 = sheet.getRow(1);
        Row r2 = sheet.getRow(2);

        Assertions.assertNotEquals(CellType.FORMULA, r1.getCell(2).getCellType());
        Assertions.assertNotEquals(CellType.FORMULA, r2.getCell(2).getCellType());

        Assertions.assertEquals("m1", r1.getCell(2).getStringCellValue());
        Assertions.assertEquals("m2", r2.getCell(2).getStringCellValue());

    }

    @Test
    @DisplayName("getModeratorRecalcHistoryWithExtra")
    void getModeratorRecalcHistoryWithExtra() throws IOException {
        byte[] res = service.getModeratorRecalcHistory(
                List.of(createModeratorRecalcDto(1), createModeratorRecalcDto(2)),
                List.of(createGoodIdDto(1), createGoodIdDto(2)),
                List.of(createModeratorSmallDto(1), createModeratorSmallDto(2)));
        XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(res));

        Assertions.assertNotNull( wb.getSheet("Moderators` Recalculation"));
        Assertions.assertNotNull(wb.getSheet("Moderators"));
        Assertions.assertNotNull(wb.getSheet("Goods"));

        Sheet sheet  = wb.getSheet("Moderators` Recalculation");
        Row header = sheet.getRow(0);
        Assertions.assertEquals("verdict", header.getCell(5).getStringCellValue());

        Row r1 = sheet.getRow(1);
        Row r2 = sheet.getRow(2);

        Assertions.assertEquals(CellType.FORMULA, r1.getCell(2).getCellType());
        Assertions.assertEquals(CellType.FORMULA, r2.getCell(2).getCellType());

        Assertions.assertEquals(String.format("VLOOKUP(B%d,Moderators!$A:$B,2,False)", 2),
                r1.getCell(2).getCellFormula());

        Assertions.assertEquals(String.format("VLOOKUP(D%d,Goods!$A:$B,2,False)",3),
                r2.getCell(4).getCellFormula());

        Sheet g = wb.getSheet("Goods");
        Sheet m = wb.getSheet("Moderators");

        Assertions.assertEquals("name", g.getRow(0).getCell(1).getStringCellValue());
        Assertions.assertEquals("username", m.getRow(0).getCell(1).getStringCellValue());

    }

    private CategoryGetDto createCategoryGetDto(int choice){
        CategoryGetDto dto = new CategoryGetDto();
        if (choice == 1 ){
            dto.setId(1l);
            dto.setName("cat1");
        }
        else{
            dto.setId(2l);
            dto.setName("cat2");
            dto.setParentId(1l);
            dto.setParentName("cat1");
        }
        return dto;
    }

    private UnitDto createUnitDto(int choice) {
        UnitDto dto = new UnitDto();
        if (choice == 1){
            dto.setId(1l);
            dto.setShortName("kg1");
            dto.setFullName("kkgg1");
        }
        else{
            dto.setId(2l);
            dto.setShortName("kg2");
            dto.setFullName("kkgg2");
        }
        return dto;
    }

    private GoodGetFullDto createGoodGetFullDto(int choice) {
        GoodGetFullDto dto = new GoodGetFullDto();
        if (choice == 1){
            dto.setId(1l);
            dto.setName("name1");
            dto.setCategory(createCategoryGetDto(1));
            dto.setTags(List.of(createTagDto(1), createTagDto(2)));
            dto.setRate(1d);
            dto.setUnit(createUnitDto(1));
        }
        else{
            dto.setId(2l);
            dto.setName("name2");
            dto.setCategory(createCategoryGetDto(2));
            dto.setTags(List.of( createTagDto(2)));
            dto.setRate(2d);
            dto.setUnit(createUnitDto(2));
        }
        dto.setCreatedAt("11.12.2025");
        dto.setUpdatedAt("11.01.2026");
        return dto;
    }



    @Test
    @DisplayName("createAllGoodsWithNoExtra")
    void createAllGoodsWithNoExtra() throws IOException {
        byte[] res = service.createAllInfoGoods(
                List.of(createGoodGetFullDto(1), createGoodGetFullDto(2)),
                null,null,null
        );

        XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(res));
        Assertions.assertNotNull( wb.getSheet("Goods"));
        Assertions.assertNull(wb.getSheet("Categories"));
        Assertions.assertNull(wb.getSheet("Units"));
        Assertions.assertNull(wb.getSheet("Tags"));

        Sheet sheet  = wb.getSheet("Goods");
        Row header = sheet.getRow(0);

        Assertions.assertEquals("good_category_name", header.getCell(3).getStringCellValue());
        Assertions.assertEquals("unit_full_name", header.getCell(7).getStringCellValue());

        Row r1 = sheet.getRow(1);
        Row r2 = sheet.getRow(2);

        Assertions.assertNotEquals(CellType.FORMULA , r1.getCell(6).getCellType());
        Assertions.assertNotEquals(CellType.FORMULA , r2.getCell(3).getCellType());

        Assertions.assertEquals("name1", r1.getCell(1).getStringCellValue());
        Assertions.assertEquals("name2", r2.getCell(1).getStringCellValue());

    }

    @Test
    @DisplayName("createAllGoodsWithAllExtra")
    void createAllGoodsWithAllExtra() throws IOException {
        byte[] res = service.createAllInfoGoods(
                List.of(createGoodGetFullDto(1), createGoodGetFullDto(2)),
                List.of(createCategoryGetDto(1), createCategoryGetDto(2)),
                List.of(createUnitDto(1), createUnitDto(2)),
                List.of(createTagDto(1), createTagDto(2))
        );

        XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(res));
        Assertions.assertNotNull( wb.getSheet("Goods"));
        Assertions.assertNotNull(wb.getSheet("Categories"));
        Assertions.assertNotNull(wb.getSheet("Units"));
        Assertions.assertNotNull(wb.getSheet("Tags"));

        Sheet sheet  = wb.getSheet("Goods");
        Row header = sheet.getRow(0);

        Assertions.assertEquals("good_category_name", header.getCell(3).getStringCellValue());
        Assertions.assertEquals("unit_full_name", header.getCell(7).getStringCellValue());

        Row r1 = sheet.getRow(1);
        Row r2 = sheet.getRow(2);

        Assertions.assertEquals(CellType.FORMULA , r1.getCell(6).getCellType());
        Assertions.assertEquals(CellType.FORMULA , r2.getCell(3).getCellType());

        Assertions.assertEquals(String.format("VLOOKUP(%s%d,Categories!$A:$D,3,FALSE)",getColumnLetter(2), 2), r1.getCell(3).getCellFormula());
        Assertions.assertEquals(String.format("VLOOKUP(%s%d,Units!$A:$C,2,FALSE)",getColumnLetter(5), 3), r2.getCell(6).getCellFormula());

        Sheet t = wb.getSheet("Tags");
        Assertions.assertEquals("ID", t.getRow(0).getCell(0).getStringCellValue());
        Assertions.assertEquals("fat1", t.getRow(1).getCell(1).getStringCellValue());

        Sheet u = wb.getSheet("Units");
        Assertions.assertEquals("short_name", u.getRow(0).getCell(1).getStringCellValue());
        Assertions.assertEquals("full_name", u.getRow(0).getCell(2).getStringCellValue());

        Sheet c = wb.getSheet("Categories");
        Assertions.assertEquals("ParentId", c.getRow(0).getCell(2).getStringCellValue());
        Assertions.assertEquals("ParentName", c.getRow(0).getCell(3).getStringCellValue());

        Cell c1 = c.getRow(1).getCell(2);
        Cell c2 = c.getRow(1).getCell(3);
        Assertions.assertTrue( c1 == null || c1.getCellType() == CellType.BLANK);
        Assertions.assertTrue( c2 == null || c2.getCellType() == CellType.BLANK);

        Assertions.assertEquals(1d, c.getRow(2).getCell(2).getNumericCellValue());
        Assertions.assertEquals("cat1", c.getRow(2).getCell(3).getStringCellValue());
    }

    public static String getColumnLetter(int index) {
        // index - 0-based (как в POI)
        StringBuilder sb = new StringBuilder();
        int col = index;
        do {
            sb.insert(0, (char) ('A' + col % 26));
            col = col / 26 - 1;
        } while (col >= 0);
        return sb.toString();
    }

}
