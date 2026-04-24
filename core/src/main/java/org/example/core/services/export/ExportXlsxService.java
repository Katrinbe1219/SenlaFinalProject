package org.example.core.services.export;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.core.dto.TagDto;
import org.example.core.dto.UnitDto;
import org.example.core.dto.export.ModeratorDto;
import org.example.core.dto.export.ShopsCurrentPricesDto;
import org.example.core.dto.getting.CategoryGetDto;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.dto.getting.goods.GoodForExportDto;
import org.example.core.dto.getting.goods.GoodForReviewDto;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.getting.rates.RateFullDto;
import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.dto.getting.statistics.shops.ShopGetDto;
import org.example.core.dto.getting.users.ModeratorSmallDto;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.service_dto.RateExportDto;
import org.example.core.utils.DateTimeUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ExportXlsxService {
    // sub-function: which are given Workbook
    // else: which creates Workbook

    private static final Logger logger = LogManager.getLogger(ExportXlsxService.class);

    private void createShopList(
            List<ShopGetDto> shops,
            Workbook wb
    ){
        try{
            Sheet sheet = wb.createSheet("Shops");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Address");
            header.createCell(3).setCellValue("District");

            for (int i =0; i< shops.size(); i++){
                ShopGetDto shop = shops.get(i);
                Row row = sheet.createRow(i+1);
                row.createCell(0).setCellValue(shop.getId());
                row.createCell(1).setCellValue(shop.getName());
                row.createCell(2).setCellValue(shop.getAddress());
                row.createCell(3).setCellValue(shop.getDistrict());
            }
        }
        catch (Exception e){
            logger.error("ExportXlsxService createShopList: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    public byte[] generateReportForCurrentShopsPrices(
            List<TagDto> tags,
            List<ShopsCurrentPricesDto> prices,
            List<ShopGetDto> shops,
            List<CategoryGetDto> categories
    ){
        try(Workbook wb = new XSSFWorkbook()){

            Sheet sheet = wb.createSheet("Prices");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Good_id");
            header.createCell(1).setCellValue("Good_name");
            header.createCell(2).setCellValue("Price");
            header.createCell(3).setCellValue("ShopId");
            int index = 4;

            if (shops != null){
                createShopList(shops, wb);
                header.createCell(index).setCellValue("Shop_name");
                index++;
                header.createCell(index).setCellValue("Shop_address");
                index++;
                header.createCell(index).setCellValue("District_Id");
                index++;
                header.createCell(index).setCellValue("District_Name");
                index++;
            }

            if (categories != null){
                createCategoryListSub(categories,wb);
                header.createCell(index).setCellValue("Category_Id");
                index++;
                header.createCell(index).setCellValue("Category_Name");
                index++;
                header.createCell(index).setCellValue("Category_Parent_Id");
                index++;
            }

            if (tags != null){
                createTagListSub(tags, wb);
                header.createCell(index).setCellValue("Tags");
            }

            for (int i=0; i< prices.size(); i++){
                ShopsCurrentPricesDto dto = prices.get(i);
                Row row = sheet.createRow(i+1);
                row.createCell(0).setCellValue(dto.getGoodId());
                row.createCell(1).setCellValue(dto.getGoodName());
                row.createCell(2).setCellValue(dto.getPrice().doubleValue());
                row.createCell(3).setCellValue(dto.getShopId());
                index = 4;

                if (shops != null){

                    header.createCell(index).setCellValue(dto.getShopName());
                    index++;
                    header.createCell(index).setCellValue(dto.getAddress());
                    index++;
                    header.createCell(index).setCellValue(dto.getDistrictId());
                    index++;
                    header.createCell(index).setCellValue(dto.getDistrictName());
                    index++;
                }

                if (categories != null){

                    header.createCell(index).setCellValue(dto.getCategoryId());
                    index++;
                    header.createCell(index).setCellValue(dto.getCategoryName());
                    index++;
                    header.createCell(index).setCellValue(dto.getCategoryParentId());
                    index++;
                }

                if (tags != null){

                    header.createCell(index).setCellValue(dto.getTags());
                }

            }

            ByteArrayOutputStream out= new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();

        }
        catch (Exception e){
            logger.error("ExportXlsxService generateReportForCurrentShopsPrices: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    public byte[] getReviewsHistory(
            List<ReviewFullDto> info,
            Map<Long, ModeratorDto> moderator,
            Map<Long, GoodForReviewDto> goods
    ){
        try(Workbook wb = new XSSFWorkbook()){
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(
                    wb.createDataFormat().getFormat("dd.mm.yyyy")
            );

            if (goods != null){
                createGoodForReviewsSub(goods,wb);
            }

            if(moderator != null){
                createModeratorForReviewsSub(moderator,wb);
            }
            Sheet sheet = wb.createSheet("Reviews");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Good_id");
            header.createCell(2).setCellValue("Good_name");
            header.createCell(3).setCellValue("user_id");
            header.createCell(4).setCellValue("username");
            header.createCell(5).setCellValue("review");
            header.createCell(6).setCellValue("rate");
            header.createCell(7).setCellValue("createdAt");
            header.createCell(8).setCellValue("blocked");
            header.createCell(9).setCellValue("blockedAt");
            header.createCell(10).setCellValue("blockedBy_id");
            header.createCell(11).setCellValue("blockedBy_username");

            for (int i = 0; i< info.size();i++){
                ReviewFullDto dto = info.get(i);
                Row row = sheet.createRow(i+1);
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(dto.getGood().getId());
                row.createCell(2).setCellValue(dto.getGood().getName());
                row.createCell(3).setCellValue(dto.getUser().getId());
                row.createCell(4).setCellValue(dto.getUser().getUsername());
                row.createCell(5).setCellValue(dto.getReview());
                row.createCell(6).setCellValue(dto.getRate());

                Cell cell= row.createCell(7);
                cell.setCellValue(DateTimeUtils.toLocalDate(dto.getCreatedAt()));
                cell.setCellStyle(dateStyle);

                row.createCell(8).setCellValue(dto.getBlocked());
                Cell cell1= row.createCell(9);

                cell1.setCellValue(DateTimeUtils.toLocalDate(dto.getBlockedAt()));
                cell1.setCellStyle(dateStyle);
                if (dto.getBlockedBy() != null){
                    row.createCell(10).setCellValue(dto.getBlockedBy().getId());
                    row.createCell(11).setCellValue(dto.getBlockedBy().getUsername());
                }

            }

            ByteArrayOutputStream out= new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();

        }
        catch (Exception e){
            logger.error("ExportXlsxService getReviewsHistory: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    public byte[] getModeratorRecalcHistory(
            List<ModeratorRecalcDto> info,
            List<GoodIdDto> goods,
            List<ModeratorSmallDto> moderators
    ){
        try(Workbook wb = new XSSFWorkbook()){
            if (goods!=null){
                createGoodForModeratorsRecalc(goods,wb);
            }
            if (moderators != null){
                createModeratorsForModeratorsRecalc(moderators,wb);
            }

            Sheet sheet = wb.createSheet("Moderators` Recalculation");
            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("moderator_id");
            header.createCell(2).setCellValue("moderator_username");
            header.createCell(3).setCellValue("good_id");
            header.createCell(4).setCellValue("good_name");
            header.createCell(5).setCellValue("verdict");
            header.createCell(6).setCellValue("comment");
            header.createCell(7).setCellValue("checked_at");

            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(
                    wb.createDataFormat().getFormat("dd.mm.yyyy hh:mm:ss")
            );

            String formula;
            Cell cell;
            for (int i =0 ; i< info.size(); i++){
                ModeratorRecalcDto dto = info.get(i);
                Row row = sheet.createRow(i+1);
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(dto.getModerator().getId());
                if (moderators != null){
                    formula = String.format("VLOOKUP(B%d,Moderators!$A:$B,2,False)", i+2);

                    row.createCell(2).setCellFormula(formula);
                }else{
                    row.createCell(2).setCellValue(dto.getModerator().getUsername());
                }

                row.createCell(3).setCellValue(dto.getGood().getId());

                if (goods != null){
                    formula = String.format("VLOOKUP(D%d,Goods!$A:$B,2,False)", i+2);
                    row.createCell(4).setCellFormula(formula);
                }else{
                    row.createCell(4).setCellValue(dto.getGood().getName());
                }

                row.createCell(5).setCellValue(dto.getVerdict().name());
                row.createCell(6).setCellValue(dto.getComment());
                cell = row.createCell(7);

                cell.setCellValue(DateTimeUtils.toLocalDateTime(dto.getCheckAt()));
                cell.setCellStyle(dateStyle);

            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();

        }
        catch (Exception e){
            logger.error("ExportXlsxService getModeratorRecalcHistory: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    private void createGoodForModeratorsRecalc(
            List<GoodIdDto> goods,
            Workbook wb
    ){
        try{
            Sheet sheet = wb.createSheet("Goods");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("name");

            int rowIndex = 1;
            for (int i=0;i< goods.size();i++) {
                Row row = sheet.createRow(rowIndex++);
                GoodIdDto dto =goods.get(i);
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(dto.getName());
            }
        }
        catch (Exception e){
            logger.error("ExportXlsxService createGoodForModeratorsRecalc: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    private void createModeratorsForModeratorsRecalc(
            List<ModeratorSmallDto> goods,
            Workbook wb
    ){
        try{
            Sheet sheet = wb.createSheet("Moderators");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("username");

            int rowIndex = 1;
            for (int i=0;i< goods.size();i++) {
                Row row = sheet.createRow(rowIndex++);
                ModeratorSmallDto dto =goods.get(i);
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(dto.getUsername());
            }
        }
        catch (Exception e){
            logger.error("ExportXlsxService createGoodForModeratorsRecalc: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    private void createGoodForReviewsSub(
            Map<Long, GoodForReviewDto> goods,
            Workbook wb
    ){
        try{
            Sheet sheet = wb.createSheet("Goods");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("name");
            header.createCell(2).setCellValue("average_rate");

            int rowIndex = 1;
            for (Map.Entry<Long, GoodForReviewDto> entry : goods.entrySet()) {
                Row row = sheet.createRow(rowIndex++);
                GoodForReviewDto dto = entry.getValue();
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(dto.getName());
                row.createCell(2).setCellValue(dto.getAverageRate());
            }
        }
        catch (Exception e){
            logger.error("ExportXlsxService createGoodForReviewsSub: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    private void createModeratorForReviewsSub(
            Map<Long, ModeratorDto> goods,
            Workbook wb
    ){
        try{
            Sheet sheet = wb.createSheet("Moderators");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("username");


            int rowIndex = 1;
            for (Map.Entry<Long, ModeratorDto> entry : goods.entrySet()) {
                Row row = sheet.createRow(rowIndex++);
                ModeratorDto dto = entry.getValue();
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(dto.getUsername());

            }
        }
        catch (Exception e){
            logger.error("ExportXlsxService createModeratorForReviewsSub: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    public byte[] createAllInfoGoods(
            List<GoodGetFullDto> goods,
            List<CategoryGetDto> categories,
            List<UnitDto> units,
            List<TagDto> tags
    ){
        try(Workbook wb = new XSSFWorkbook()){
            if (categories != null){
                createCategoryListSub(categories, wb);
            }

            if (units != null){
                createUnitListSub(units, wb);
            }

            if (tags != null){
                createTagListSub(tags, wb);
            }


            Sheet sheet = wb.createSheet("Goods");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("good_id");
            header.createCell(1).setCellValue("good_name");
            header.createCell(2).setCellValue("good_category_id");
            header.createCell(3).setCellValue("good_category_name");
            header.createCell(4).setCellValue("good_category_parent");
            header.createCell(5).setCellValue("unit_id");
            header.createCell(6).setCellValue("unit_short_name");
            header.createCell(7).setCellValue("unit_full_name");
            header.createCell(8).setCellValue("rate");
            header.createCell(9).setCellValue("tags");
            header.createCell(10).setCellValue("createdAt");
            header.createCell(11).setCellValue("updatedAt");

            for (int i=0; i<goods.size(); i++){
                GoodGetFullDto dto = goods.get(i);
                Row row = sheet.createRow(i+1);
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(dto.getName());
                row.createCell(2).setCellValue(dto.getCategory().getId());
                row.createCell(3).setCellValue(dto.getCategory().getName());
                row.createCell(4).setCellValue(dto.getCategory().getParent());
                row.createCell(5).setCellValue(dto.getUnit().getId());
                row.createCell(6).setCellValue(dto.getUnit().getShortName());
                row.createCell(7).setCellValue(dto.getUnit().getFullName());
                row.createCell(8).setCellValue(dto.getRate());
                if (dto.getTags() != null){
                    String value = String.join(",", dto.getTags().stream().map(TagDto::getName).toList());
                    row.createCell(9).setCellValue(value);
                }
                row.createCell(10).setCellValue(dto.getCreatedAt());
                row.createCell(11).setCellValue(dto.getUpdatedAt());
            }


            ByteArrayOutputStream out= new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();


        }
        catch (Exception e){
            logger.error("ExportXlsxService createAllInfoGoods: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    public byte[] getRecalculations(
            List<RateExportDto> rates,
            List<GoodForExportDto> goods,
            List<CategoryGetDto> categories
    ){
        try(Workbook wb = new XSSFWorkbook()){
            if (categories != null){
                createCategoryNameListSub(categories, wb);
            }
            if (goods != null){
                createGoodNameListSub(goods, categories!=null,wb);
            }

            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(
                    wb.createDataFormat().getFormat("dd.mm.yyyy")
            );

            Sheet sheet = wb.createSheet("Recalculations");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("recalculation_id");
            header.createCell(1).setCellValue("good_id");
            header.createCell(2).setCellValue("good_name");
            header.createCell(3).setCellValue("category_id");
            header.createCell(4).setCellValue("category_name");
            header.createCell(5).setCellValue("recalculated_at");
            header.createCell(6).setCellValue("triggered_by");
            header.createCell(7).setCellValue("rating_status");
            header.createCell(8).setCellValue("error_message");
            header.createCell(9).setCellValue("rate");

            String formula;
            for (int i=0; i<rates.size(); i++){
                RateExportDto dto = rates.get(i);
                Row row = sheet.createRow(i+1);
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(dto.getGoodId());
                if (goods!=null){
                    formula = String.format("VLOOKUP(B%d,Goods!$A:$D,2,False)", i+2);
                    row.createCell(2).setCellFormula(formula);
                    formula = String.format("VLOOKUP(B%d,Goods!$A:$D,3,False)",i+2);
                    row.createCell(3).setCellFormula(formula);
                    formula = String.format("VLOOKUP(B%d,Goods!$A:$D,4,False)",i+2);
                    row.createCell(4).setCellFormula(formula);
                }
                else{
                    row.createCell(2).setCellValue(dto.getGoodName());
                    row.createCell(3).setCellValue(dto.getCategoryId());
                    row.createCell(4).setCellValue(dto.getCategoryName());
                }

                Cell cell = row.createCell(5);
                cell.setCellStyle(dateStyle);
                cell.setCellValue(DateTimeUtils.toLocalDateTime(dto.getRecalculatedAt()));

                row.createCell(6).setCellValue(dto.getTriggeredBy().name());
                row.createCell(7).setCellValue(dto.getRatingStatus().name());
                row.createCell(8).setCellValue(dto.getErrorMessage());
                row.createCell(9).setCellValue(dto.getRate());


            }

            ByteArrayOutputStream out= new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();

        }
        catch (Exception e){
            logger.error("ExportXlsxService getRecalculations: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    private void createTagListSub(
            List<TagDto> tags,
            Workbook wb
    ){
        try{
            Sheet sheet = wb.createSheet("Tags");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");

            for (int i = 0; i < tags.size(); i++) {
                TagDto tag = tags.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(tag.getId());
                row.createCell(1).setCellValue(tag.getName());
            }
        }
        catch (Exception e){
            logger.error("ExportXlsxService createTagList: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    private void createUnitListSub(
            List<UnitDto> tags,
            Workbook wb
    ){
        try{
            Sheet sheet = wb.createSheet("Units");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("short_name");
            header.createCell(1).setCellValue("full_name");

            for (int i = 0; i < tags.size(); i++) {
                UnitDto tag = tags.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(tag.getId());
                row.createCell(1).setCellValue(tag.getShortName());
                row.createCell(1).setCellValue(tag.getFullName());
            }
        }
        catch (Exception e){
            logger.error("ExportXlsxService createUnitListSub: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }


    private void createCategoryListSub(
            List<CategoryGetDto> categories,
            Workbook wb
    ){
        try{
            Sheet sheet = wb.createSheet("Categories");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("ParentId");

            for (int i =0; i < categories.size(); i++){
                CategoryGetDto category = categories.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(category.getId());
                row.createCell(1).setCellValue(category.getName());
                row.createCell(2).setCellValue(category.getParent());
            }
        }
        catch (Exception e){
            logger.error("ExportXlsxService createCategoryList: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    private void createCategoryNameListSub(
            List<CategoryGetDto> cats,
            Workbook wb
    ){
        try{
            Sheet sheet = wb.createSheet("Categories");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");

            for (int i=0; i<cats.size(); i++){
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(cats.get(i).getId());
                row.createCell(1).setCellValue(cats.get(i).getName());
            }
        }
        catch (Exception e){
            logger.error("ExportXlsxService createCategoryNameListSub: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }


    }

    private void createGoodNameListSub(
            List<GoodForExportDto> goods,
            boolean isCategory,
            Workbook wb
    ){
        try{
            Sheet sheet = wb.createSheet("Goods");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("category_id");
            header.createCell(3).setCellValue("category_name");


            for (int i=0; i<goods.size(); i++){
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(goods.get(i).getId());
                row.createCell(1).setCellValue(goods.get(i).getName());
                row.createCell(2).setCellValue(goods.get(i).getCategoryId());
                if (!isCategory){
                    row.createCell(3).setCellValue(goods.get(i).getCategoryName());
                }else{
                    // в самом экселе счет идет с 1, а не с 0, поэтому прибавляем 2
                    String formula = String.format(
                            "VLOOKUP(C%d,Categories!$A:$B,2,FALSE)", i+2
                    );
                    row.createCell(3).setCellFormula(formula);
                }


            }
        }
        catch (Exception e){
            logger.error("ExportXlsxService createGoodNameListSub: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }


    }

}
