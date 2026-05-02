package org.example.core.controllers.system;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.TagDto;
import org.example.core.dto.UnitDto;
import org.example.core.dto.export.ModeratorDto;
import org.example.core.dto.export.PriceHistoryByGoodAndShop;
import org.example.core.dto.export.ShopsCurrentPricesDto;
import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.getting.users.ModeratorSmallDto;
import org.example.core.dto.getting.users.UserForReviewDto;
import org.example.core.dto.getting.goods.GoodForExportDto;
import org.example.core.dto.getting.goods.GoodForReviewDto;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.dto.getting.statistics.shops.ShopGetDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.exportEnum.*;
import org.example.core.hibernate.base_settings.filters.ModeratorRecalcFilter;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.hibernate.base_settings.filters.exporting.ExportShopsCurrentPricesFilter;
import org.example.core.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewAdvancedFilters;
import org.example.core.hibernate.base_settings.service_dto.RateExportDto;
import org.example.core.services.dictionaries.CategoryService;
import org.example.core.services.dictionaries.TagService;
import org.example.core.services.documents.ModeratorRecalcService;
import org.example.core.services.documents.RateService;
import org.example.core.services.documents.reviews.ReviewAdvancedService;
import org.example.core.services.documents.prices.PriceExportService;
import org.example.core.services.export.ExportCsvService;
import org.example.core.services.export.ExportXlsxService;
import org.example.core.services.objects.GoodService;
import org.example.core.services.objects.ShopService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/export")
@AllArgsConstructor
public class ExportController {

    private CategoryService categoryService;
    private ReviewAdvancedService reviewService;
    private PriceExportService priceService;
    private TagService tagService;
    private ShopService shopService;

    private ExportXlsxService xlsxService;
    private ExportCsvService csvService;

    private GoodService goodService;
    private RateService rateService;
    private ModeratorRecalcService moderatorRecalcService;





    @GetMapping("/prices")
    public ResponseEntity<byte[]> getShopsCurrentPrices(
            @RequestParam(value = "format", defaultValue = "csv", required = false) String format,
            @RequestParam(value="include",  required = false) List<String> include,
            @RequestParam(value="shopIds", required = false) List<Long> shopIds
    ){
        ExportShopsCurrentPricesFilter filters = new ExportShopsCurrentPricesFilter();
        Set<ShopsCurrentPricesIncludeTypes> includes = include == null
                ? Set.of()
                : include.stream().map(ShopsCurrentPricesIncludeTypes::fromString).collect(Collectors.toSet());

        boolean shopsAreIncluded = includes.contains(ShopsCurrentPricesIncludeTypes.SHOPS);
        boolean categoriesAreIncluded = includes.contains(ShopsCurrentPricesIncludeTypes.CATEGORIES);

        if (categoriesAreIncluded){
            filters.setCategories(true);
        }

        if (shopsAreIncluded){
            filters.setShops(true);
        }

        if (shopIds == null || !shopIds.isEmpty()){
            filters.setShopsIds(shopIds);
        }

        boolean tagsAreIncluded= includes.contains(ShopsCurrentPricesIncludeTypes.TAGS);

        if (tagsAreIncluded){
            filters.setTags(true);
        }

        List<ShopsCurrentPricesDto> info = priceService.getShopsCurrentPrices(filters);
        if (info.isEmpty()){
            throw new DoesNoeExist("Nothing was found");
        }



        ExportFormat formatType = ExportFormat.getFormat(format);
        HttpHeaders headers = new HttpHeaders();
        byte[] excelBytes = null;


        switch (formatType){
            case CSV -> {
                excelBytes = csvService.getShopsCurrentPrices(info, filters);
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDisposition(
                        ContentDisposition.attachment().filename("report.csv").build()
                );
            }
            case XLSX -> {
                List<TagDto> tags = null;
                List<ShopGetDto> shops = null;
                List<CategoryGetDto> categories = null;
                if (tagsAreIncluded){
                    tags = tagService.getAllTags(null, null);
                }
                if (shopsAreIncluded){
                    shops = shopService.findAll(null, null);
                }
                if (categoriesAreIncluded){
                    categories = categoryService.getAllCategories(null, null);
                }

                headers.setContentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ));
                headers.setContentDisposition(
                        ContentDisposition.attachment().filename("report.xlsx").build()
                );

                excelBytes = xlsxService.generateReportForCurrentShopsPrices(tags, info, shops, categories);


            }
        }

        headers.setContentLength(excelBytes.length);
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);


    }



    @GetMapping("/prices/history/{id}")
    public ResponseEntity<byte[]> getPriceHistoryByGoodId(
            @PathVariable("id") Long goodId,
            @RequestParam("shopId") Long shopId
    ){
        List<PriceHistoryByGoodAndShop> prices = priceService.getPriceHistoryByGoodId(goodId, shopId);
        if (prices.isEmpty()){
            throw new DoesNoeExist("Nothing was found");
        }
        byte[] exportBytes = csvService.getPriceHistoryByGoodId(prices);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("report.csv").build()
        );
        headers.setContentLength(exportBytes.length);
        return new ResponseEntity<>(exportBytes, headers, HttpStatus.OK);

    }

    @GetMapping("/reviews/history")
    public ResponseEntity<byte[]> getReviewsHistory(
            @RequestParam(value = "include", required = false) List<String> include,
            @RequestParam(value = "format", defaultValue = "csv") String format,
            @Valid @RequestBody ReviewAdvancedFilters filters
    ){


        ExportFormat formatType = ExportFormat.getFormat(format);

        if (include != null && !include.isEmpty() && formatType == ExportFormat.CSV){
            throw new NotCorrectInput("Csv can not be applied with include");
        }

        filters.setPage(null);
        filters.setSize(null);
        List<ReviewFullDto> reviews = reviewService.getByFilters(filters);

        if (reviews.isEmpty()){
            throw new DoesNoeExist("Nothing was found");
        }

        HttpHeaders headers = new HttpHeaders();

        Set<ReviewsHistoryInclude> includes = include == null
                ? Set.of()
                : include.stream().map(ReviewsHistoryInclude::fromString).collect(Collectors.toSet());

        boolean moderatorsAreIncluded = includes.contains(ReviewsHistoryInclude.MODERATORS);
        boolean goodsAreIncluded = includes.contains(ReviewsHistoryInclude.GOODS);




        byte[] exportBytes= null;
        switch (formatType){
            case CSV -> {
                exportBytes = csvService.getReviewsHistory(reviews);
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDisposition(
                        ContentDisposition.attachment().filename("report.csv").build()
                );
            }
            case XLSX -> {
                Map<Long, ModeratorDto> moderators = null;
                Map<Long,GoodForReviewDto> goods = null;

                if (moderatorsAreIncluded){
                    moderators = reviews.stream()
                            .map(ReviewFullDto::getBlockedBy)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toMap(
                                    UserForReviewDto::getId,
                                    u -> new ModeratorDto(u.getId(), u.getUsername()),
                                    (existing, duplicate) -> existing
                            ));
                }

                if (goodsAreIncluded){
                    goods = reviews.stream().map(
                            ReviewFullDto::getGood
                    ).collect(Collectors.toMap(
                            GoodForReviewDto::getId, u-> u
                    ));
                }

                exportBytes = xlsxService.getReviewsHistory(reviews, moderators, goods);
                headers.setContentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ));
                headers.setContentDisposition(
                        ContentDisposition.attachment().filename("report.xlsx").build()
                );
            }
        }
        headers.setContentLength(exportBytes.length);
        return new ResponseEntity<>(exportBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/reviews/history/{id}")
    public ResponseEntity<byte[]> getReviewsHistoryByGoodId(
            @PathVariable("id") Long goodId,
            @Valid @RequestBody ReviewAdvancedFilters filters

    ){

        filters.setPage(null);
        filters.setSize(null);
        filters.setGoodId(goodId);
        List<ReviewFullDto> reviews = reviewService.getByFilters(filters);

        if (reviews.isEmpty()){
            throw new DoesNoeExist("Nothing was found");
        }

        byte[] exportBytes = csvService.getReviewsHistoryByGoodId(reviews);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("report.csv").build()
        );

        headers.setContentLength(exportBytes.length);
        return new ResponseEntity<>(exportBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/recalculations/history")
    public ResponseEntity<byte[]> getRecalculationsHistory(
            @Valid @RequestBody RatingRecalcFilter filters,
            @RequestParam(value = "format", defaultValue = "csv", required = false) String format,
            @RequestParam(value="include", required = false) List<String> include
    ){

        ExportFormat formatType = ExportFormat.getFormat(format);

        Set<RecalculationInclude> includes = include == null ?
                Set.of()
                : include.stream().map(RecalculationInclude::fromString).collect(Collectors.toSet());

        boolean goodsAreIncluded = includes.contains(RecalculationInclude.GOODS);
        boolean categoriesAreIncluded = includes.contains(RecalculationInclude.CATEGORIES);

        List<RateExportDto> rates = rateService.getRatesExportByFilter(filters);
        if (rates.isEmpty()){
            throw new DoesNoeExist("Nothing was found");
        }
        HttpHeaders headers = new HttpHeaders();
        byte[] exportBytes = null;

        switch(formatType){
            case CSV -> {
                exportBytes = csvService.getRecalculations(rates);
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDisposition(
                        ContentDisposition.attachment().filename("recalculations.csv").build()
                );
            }
            case XLSX -> {
                List<GoodForExportDto> goods = null;
                List<CategoryGetDto> categories = null;

                if (goodsAreIncluded){
                    goods = rates.stream().map(
                            t -> {
                                GoodForExportDto dto = new GoodForExportDto();
                                dto.setId(t.getGoodId());
                                dto.setName(t.getGoodName());
                                dto.setCategoryId(t.getCategoryId());
                                dto.setCategoryName(t.getCategoryName());
                                return dto;
                            }
                    ).collect(Collectors.toMap(
                            GoodForExportDto::getId,
                            val -> val,
                                    (old,n) -> old
                            )).values()
                            .stream()
                            .toList();
                }

                if (categoriesAreIncluded){
                    categories = rates.stream()
                            .map(
                            t -> {
                                CategoryGetDto dto = new CategoryGetDto();
                                dto.setId(t.getCategoryId());
                                dto.setName(t.getCategoryName());
                                return dto;
                            }
                    ).collect(Collectors.toMap(
                            CategoryGetDto::getId,
                                    b -> b,
                                    (a,c) -> a
                            )).values().stream().toList();
                }

                exportBytes = xlsxService.getRecalculations(rates, goods, categories);

                headers.setContentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ));
                headers.setContentDisposition(
                        ContentDisposition.attachment().filename("recalculations.xlsx").build()
                );
            }
        }

        headers.setContentLength(exportBytes.length);


        return new ResponseEntity<>(exportBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/goods")
    public ResponseEntity<byte[]> getAllInfoGoods(
            @RequestParam(value = "format", defaultValue = "csv", required = false) String format,
            @Valid @RequestBody GoodFilter filters,
            @RequestParam(value = "include", required = false)List<String> include
    ){
        filters.setPage(null);
        filters.setSize(null);

        Set<AllGoodsInclude> includes = include == null
                ? Set.of()
                : include.stream().map(AllGoodsInclude::getEnum).collect(Collectors.toSet());

        List<GoodGetFullDto> goods = goodService.findAllForAnalyst(filters);
        if (goods.isEmpty()){
            throw new DoesNoeExist("Nothing was found");
        }

        ExportFormat formatType = ExportFormat.getFormat(format);
        byte[] exportBytes = null;
        HttpHeaders headers = new HttpHeaders();

        boolean tagsAreIncluded = includes.contains(AllGoodsInclude.TAGS);
        boolean categoriesAreIncluded = includes.contains(AllGoodsInclude.CATEGORIES);
        boolean unitsAreIncluded = includes.contains(AllGoodsInclude.UNITS);

        switch (formatType){
            case XLSX -> {


                List< TagDto> tags = null;
                List<UnitDto> units = null;
                List<CategoryGetDto> categories = null;
                if (tagsAreIncluded){
                    tags = goods.stream()
                            .filter(g -> g.getTags() != null)
                            .flatMap(g -> g.getTags().stream())
                            .distinct() // о аннотации lombok equalsandHasCode поставили по id полю
                            .toList();
                }

                if (unitsAreIncluded){
                    units = goods.stream()
                            .map(GoodGetFullDto::getUnit)
                            .collect(Collectors.toMap(
                                    UnitDto::getId,
                                    u -> u,
                                    (a, b) -> a
                            ))
                            .values()
                            .stream()
                            .toList();
                }

                if (categoriesAreIncluded){
                    categories = goods.stream()
                            .map(GoodGetFullDto::getCategory)
                            .collect(Collectors.toMap(
                                    CategoryGetDto::getId,
                                    c -> c,
                                    (a, b) -> a
                            ))
                            .values()
                            .stream()
                            .toList();
                }

                exportBytes = xlsxService.createAllInfoGoods(goods, categories, units, tags);


                headers.setContentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ));
                headers.setContentDisposition(
                        ContentDisposition.attachment().filename("all_goods.xlsx").build()
                );
            }
            case CSV -> {
                exportBytes = csvService.getAllGoods(goods);

                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDisposition(
                        ContentDisposition.attachment().filename("all_goods.csv").build()
                );
            }
        }


        headers.setContentLength(exportBytes.length);
        return new ResponseEntity<>(exportBytes, headers, HttpStatus.OK);
    }


    @GetMapping("/moderators-recalculations")
    public ResponseEntity<byte[]> getModeratorRecalc(
            @RequestParam(value = "format", defaultValue = "csv", required = false) String format,
            @RequestParam(name = "include", required = false) List<String> include,
            @Valid @RequestBody ModeratorRecalcFilter filters
    ){
        Set<ModeratorRecalcInclude> includes = include == null ? Set.of()
                : include.stream().map(ModeratorRecalcInclude::getFormat).collect(Collectors.toSet());

        ExportFormat formatType = ExportFormat.getFormat(format);

        byte[] exportBytes = null;
        HttpHeaders headers = new HttpHeaders();

        List<ModeratorRecalcDto> info = moderatorRecalcService.findAllFullVersion(filters);

        switch(formatType){
            case CSV -> {
                exportBytes = csvService.getModeratorRecalc(info);

                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDisposition(
                        ContentDisposition.attachment().filename("moderators-recalculations.csv").build()
                );
            }
            case XLSX -> {
                List<ModeratorSmallDto> moderatorDtos = null;
                List<GoodIdDto> goodDtos = null;

                if (includes.contains(ModeratorRecalcInclude.MODERATORS)){
                    moderatorDtos = info.stream().map(ModeratorRecalcDto::getModerator)
                            .collect(Collectors.toMap(
                                    ModeratorSmallDto::getId,
                                    dto -> dto,
                                    (old, n) -> old
                            ))
                            .values()
                            .stream()
                            .toList();
                }
                if (includes.contains(ModeratorRecalcInclude.GOODS)){

                    goodDtos = info.stream().map(ModeratorRecalcDto::getGood)
                            .collect(Collectors.toMap(
                                    GoodIdDto::getId,
                                    dto->dto,
                                    (old, n) -> old
                            )).values()
                            .stream().toList();
                }

                exportBytes = xlsxService.getModeratorRecalcHistory(info, goodDtos, moderatorDtos);
                headers.setContentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ));
                headers.setContentDisposition(
                        ContentDisposition.attachment().filename("moderators-recalculations.xlsx").build()
                );
            }
        }
        headers.setContentLength(exportBytes.length);
        return new ResponseEntity<>(exportBytes, headers, HttpStatus.OK);

    }



}
