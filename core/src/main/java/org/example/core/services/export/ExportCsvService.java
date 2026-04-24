package org.example.core.services.export;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.example.core.dto.TagDto;
import org.example.core.dto.export.ModeratorDto;
import org.example.core.dto.export.PriceHistoryByGoodAndShop;
import org.example.core.dto.export.ShopsCurrentPricesDto;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.dto.getting.rates.RateFullDto;
import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.filters.exporting.ExportShopsCurrentPricesFilter;
import org.example.core.hibernate.base_settings.service_dto.RateExportDto;
import org.example.core.utils.DateTimeUtils;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExportCsvService {

    private static final Logger logger = LogManager.getLogger(ExportCsvService.class);
    private final String DELIMITER = ";";

    public byte[] getShopsCurrentPrices(List<ShopsCurrentPricesDto> info,
                                        ExportShopsCurrentPricesFilter filters){
        try{
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            List<String> headers = new ArrayList<>();
            headers.add("good_id");
            headers.add("good_name");
            headers.add("price");
            headers.add("shop_id");
            if (filters.isShops()) {
                headers.add("shop_name");
                headers.add("address");
                headers.add("district_id");
                headers.add("district_name");
            }
            if (filters.isCategories()) {
                headers.add("category_id");
                headers.add("category_name");
                headers.add("category_parent_id");
            }
            if (filters.isTags()) {
                headers.add("tags");
            }
            pw.println(String.join(DELIMITER, headers));


            for(ShopsCurrentPricesDto item : info){
                List<String> fields = new ArrayList<String>();
                fields.add(String.valueOf(item.getGoodId()));
                fields.add(String.valueOf(item.getGoodName()));
                fields.add(String.valueOf(item.getPrice()));
                fields.add(String.valueOf(item.getShopId()));

                if (filters.isShops()){
                    fields.add(String.valueOf(item.getShopName()));
                    fields.add(String.valueOf(item.getAddress()));
                    fields.add(String.valueOf(item.getDistrictId()));
                    fields.add(String.valueOf(item.getDistrictName()));
                }

                if (filters.isCategories()){
                    fields.add(String.valueOf(item.getCategoryId()));
                    fields.add(String.valueOf(item.getCategoryName()));
                    fields.add(String.valueOf(item.getCategoryParentId()));
                }
                if (filters.isTags()){
                    fields.add(escapeCsv(String.valueOf(item.getTags())));
                }

                pw.println(String.join(DELIMITER, fields));
            }
            pw.flush();
            return sw.toString().getBytes(Charset.forName("Windows-1251"));


        }
        catch (Exception e){
            logger.error("ExportCsvService getShopsCurrentPrices:" + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    private String escapeCsv(String value){
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"")
        || value.contains("\n")){
            return "\"" + value.replace("\"","\"\"") + "\"";
        }

        return value;
    }

    public byte[] getModeratorRecalc(
            List<ModeratorRecalcDto> info
    ){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("recalculation_id;moderator_id;moderator_username;good_id;good_name;comment;checked_at");
        for (ModeratorRecalcDto dto:info){
            List<String> fields = new ArrayList<>();
            fields.add(String.valueOf(dto.getId()));
            fields.add(String.valueOf(dto.getModerator().getId()));
            fields.add(String.valueOf(dto.getModerator().getUsername()));
            fields.add(String.valueOf(dto.getGood().getId()));
            fields.add(String.valueOf(dto.getGood().getName()));
            fields.add(String.valueOf(dto.getComment()));
            fields.add(String.valueOf(  String.valueOf(DateTimeUtils.toLocalDateTime(dto.getCheckAt()  )) ));
            pw.println(String.join(DELIMITER, fields));
        }

        pw.flush();
        return sw.toString().getBytes(Charset.forName("Windows-1251"));

    }
    public byte[] getPriceHistoryByGoodId(
            List<PriceHistoryByGoodAndShop> info
    ){
        try{
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            pw.println("id;price;validFrom;validTo");
            for (PriceHistoryByGoodAndShop item : info){
                List<String> fields = new ArrayList<>();
                fields.add(String.valueOf(item.getPriceId()));
                fields.add(String.valueOf(item.getPrice()));
                fields.add(String.valueOf(item.getValidFrom()));
                fields.add(String.valueOf(String.valueOf(DateTimeUtils.toLocalDateTime(item.getValidTo()))));

                pw.println(String.join(DELIMITER, fields));
            }

            pw.flush();
            return sw.toString().getBytes(Charset.forName("Windows-1251"));
        }
        catch (Exception e){
            logger.error("ExportCsvService getPriceHistoryByGoodId:" + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    public byte[] getReviewsHistoryByGoodId(
            List<ReviewFullDto> info
    ){
        try{
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("id;user_id;user_username;review;rate;createdAt;blocked;blockedAt;blocked_by_id;blocked_by_username");

            for (ReviewFullDto item : info){
                List<String> fields = new ArrayList<>();
                fields.add(String.valueOf(item.getId()));
                fields.add(String.valueOf(item.getUser().getId()));
                fields.add(String.valueOf(item.getUser().getUsername()));
                fields.add(String.valueOf(item.getReview()));
                fields.add(String.valueOf(item.getRate()));
                fields.add(String.valueOf(DateTimeUtils.toLocalDate(item.getCreatedAt())));
                fields.add(String.valueOf(item.getBlocked()));
                fields.add(String.valueOf(DateTimeUtils.toLocalDate(item.getBlockedAt())));
                if (item.getBlockedBy() != null){
                    fields.add(String.valueOf(item.getBlockedBy().getId()));
                    fields.add(String.valueOf(item.getBlockedBy().getUsername()));
                }else{
                    fields.add("null");
                    fields.add("null");
                }
                pw.println(String.join(DELIMITER, fields));

            }

            pw.flush();
            return sw.toString().getBytes(Charset.forName("Windows-1251"));

        }
        catch (Exception e){
            logger.error("ExportCsvService getReviewsHistoryByGoodId:" + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    public byte[] getReviewsHistory(
            List<ReviewFullDto> info
    ){
        try{
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("id;good_id;good_name;user_id;user_username;review;rate;createdAt;blocked;blockedAt;blocked_by_id;blocked_by_username");

            for (ReviewFullDto item : info){
                List<String> fields = new ArrayList<>();
                fields.add(String.valueOf(item.getId()));
                fields.add(String.valueOf(item.getGood().getId()));
                fields.add(String.valueOf(item.getGood().getName()));
                fields.add(String.valueOf(item.getUser().getId()));
                fields.add(String.valueOf(item.getUser().getUsername()));
                fields.add(String.valueOf(item.getReview()));
                fields.add(String.valueOf(item.getRate()));
                fields.add(String.valueOf(DateTimeUtils.toLocalDate(item.getCreatedAt())));
                fields.add(String.valueOf(item.getBlocked()));
                if (item.getBlockedBy() != null){
                    fields.add(String.valueOf(DateTimeUtils.toLocalDate(item.getBlockedAt())));
                }else{
                    fields.add("null");
                }

                if (item.getBlockedBy() != null){
                    fields.add(String.valueOf(item.getBlockedBy().getId()));
                    fields.add(String.valueOf(item.getBlockedBy().getUsername()));
                }else{
                    fields.add("null");
                    fields.add("null");
                }
                pw.println(String.join(DELIMITER, fields));

            }

            pw.flush();
            return sw.toString().getBytes(Charset.forName("Windows-1251"));

        }
        catch (Exception e){
            logger.error("ExportCsvService getReviewsHistory:" + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    public byte[] getAllGoods(
            List<GoodGetFullDto> info
    ){
        try{
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("id;good_name;category_id;category_name;category_parent_name;unit_id;unit_short_name;unit_full_name;description;tags;updated_at;created_at");

            for (GoodGetFullDto item : info){
                List<String> fields = new ArrayList<>();
                fields.add(String.valueOf(item.getId()));
                fields.add(String.valueOf(item.getName()));
                fields.add(String.valueOf(item.getCategory().getId()));
                fields.add(String.valueOf(item.getCategory().getName()));
                fields.add(String.valueOf(item.getCategory().getParent()));
                fields.add(String.valueOf(item.getUnit().getId()));
                fields.add(String.valueOf(item.getUnit().getShortName()));
                fields.add(String.valueOf(item.getUnit().getFullName()));
                fields.add(String.valueOf(item.getDescription()));
                if (item.getTags() != null){
                    String tags = item.getTags().stream().map(TagDto::getName).collect(Collectors.joining(","));
                    fields.add(escapeCsv(tags));
                }else{
                    fields.add("null");
                }
                fields.add(String.valueOf(item.getUpdatedAt()));
                fields.add(String.valueOf(item.getCreatedAt()));
                pw.println(String.join(DELIMITER, fields));

            }

            pw.flush();
            return sw.toString().getBytes(Charset.forName("Windows-1251"));

        }
        catch (Exception e){
            logger.error("ExportCsvService getAllGoods:" + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    public byte[] getRecalculations(
            List<RateExportDto> rates
    ){
        try{
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            pw.println("id;good_id;good_name;category_id;category_name;recalculated_at;triggeredBy;rating_status;errorMessage;rate");
            for (RateExportDto item : rates){
                List<String> fields = new ArrayList<>();
                fields.add(String.valueOf(item.getId()));
                fields.add(String.valueOf(item.getGoodId()));
                fields.add(String.valueOf(item.getGoodName()));
                fields.add(String.valueOf(item.getCategoryId()));
                fields.add(String.valueOf(item.getCategoryName()));
                fields.add(String.valueOf( DateTimeUtils.toLocalDateTime(item.getRecalculatedAt())  ));
                fields.add(String.valueOf(item.getTriggeredBy()));
                fields.add(String.valueOf(item.getRatingStatus()));
                fields.add(String.valueOf(item.getErrorMessage()));
                fields.add(String.valueOf(item.getRate()));

                pw.println(String.join(DELIMITER, fields));

            }

            pw.flush();
            return sw.toString().getBytes(Charset.forName("Windows-1251"));

        }
        catch (Exception e){
            logger.error("ExportCsvService getRecalculations:" + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }
}
