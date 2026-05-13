package org.example.core.controllers.prices;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.getting.goods.GoodAnalyseForShopDto;
import org.example.core.dto.getting.goods.GoodPriceInShop;
import org.example.core.dto.getting.prices.PriceInTime;
import org.example.core.dto.getting.statistics.CartStatisticRequest;
import org.example.core.dto.getting.statistics.DistrictStatisticDto;
import org.example.core.dto.getting.statistics.categories.CategoryStatDto;
import org.example.core.dto.getting.statistics.shops.ShopCartDto;
import org.example.core.dto.getting.statistics.shops.ShopStatByCategoryDto;
import org.example.core.dto.getting.statistics.shops.ShopStatisticDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NoDataForContentException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.goods.GoodPriceInShopsFilter;
import org.example.core.hibernate.base_settings.filters.prices.DistrictStatisticFilter;
import org.example.core.hibernate.base_settings.filters.prices.PriceInTimeFilter;
import org.example.core.hibernate.base_settings.filters.prices.ShopStatByCategoryFilter;
import org.example.core.services.documents.prices.PriceAnalyzeService;
import org.example.core.services.graphics.GraphicalAnalyseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/analyst/prices")
@AllArgsConstructor
//TODO Exception went to JwtFilter and causes Not authorize!!!!!!!
public class PriceForAnalyzeController {

    private PriceAnalyzeService priceService;
    private GraphicalAnalyseService graphicService;



    @GetMapping("/shops/{id}/goods")
    // получение максимальных и минимальных продуктов на данный момент p.validTO is NULL
    public List<GoodAnalyseForShopDto> getGoodsByShopId(
            @PathVariable("id") Long id,
            @RequestParam(value = "type", defaultValue = "max", required = false) String type,
            @RequestParam(value = "count", defaultValue = "4", required = false) int count
    ) {

        if (id <=0){
            throw new NotCorrectInput("path variable id must be > 0");

        }
        if (count <=0){
            throw new NotCorrectInput("Request Param count must be > 0");
        }

        if (!type.equalsIgnoreCase("max") && !type.equalsIgnoreCase("min")) {
            throw new NotCorrectInput("Request param type  must be min or max");
        }
        return priceService.getGoodsByShop(type, id, count);

    }

    @GetMapping(value = "/shops/good/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void getGoodPricesInShops(
            HttpServletResponse response,
            @Valid @RequestBody GoodPriceInShopsFilter filters
    ) throws Exception {

        List<GoodPriceInShop> prices = priceService.getGoodPricesInShops(filters);

        if (prices.isEmpty()){
            throw  new NoDataForContentException("No prices were found to generate graph");
        }

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");


        graphicService.generateBarForGoodPriceInShops(
                response.getOutputStream(), "Good Price in shops", "shops", "prices", prices
        );

        response.getOutputStream().flush();

    }

    @GetMapping(value = "/shops/good-in-time", produces = MediaType.IMAGE_PNG_VALUE)
    public void getGoodsByShopIdInTime(
            HttpServletResponse response,
            @Valid @RequestBody PriceInTimeFilter filters
    ) throws Exception {


        List<PriceInTime> prices =  priceService.getGoodPriceInTime(filters);
        if (prices.isEmpty()){
            throw new NoDataForContentException("No prices were found to generate graph");
        }
        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");
        graphicService.generateTimeSeriesForGoodPriceInShopTime(
                response.getOutputStream(), "Prices", "Date", "Prices", prices, filters
        );

        response.getOutputStream().flush();

    }

    @GetMapping("/categories/main")
    //могут быть все - без параметров
    // могут быть определенные через параметры
    public List<ShopStatByCategoryDto> getShopsStatsByMainCategories(
           @Valid @RequestBody ShopStatByCategoryFilter filters
    ){

        return priceService.getShopsStatByMainCategories(filters);
    }

    @GetMapping(value = "/categories/main/shop/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void getShopStatsByMainShopIdGraph(
            HttpServletResponse response,
            @Valid @RequestBody ShopStatByCategoryFilter filters
    ) throws Exception {

        if (filters.getShopIds() == null || filters.getShopIds().size() !=1){
            throw  new NotCorrectInput("shopIds length must be 1");
        }


        List<ShopStatByCategoryDto> categories = priceService.getShopsStatByMainCategories(filters);
        if (categories.isEmpty()){
            throw new NoDataForContentException("Nothing was found with given credentials");
        }

        List<CategoryStatDto> stats = categories.get(0).getCategories().stream()
                .sorted(Comparator.comparing(CategoryStatDto::getCategoryId)).toList();

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");

        graphicService.generateBarForAverageCategoriesRate(
                response.getOutputStream(), "Main Categories Avg Prices", "categories` ids",
                "prices", stats
        );

        response.getOutputStream().flush();

    }

    @GetMapping("/categories/sub")
    public List<ShopStatByCategoryDto> getShopsStatsBySubCategories(
            @Valid @RequestBody ShopStatByCategoryFilter filters
    ){

        return priceService.getShopsStatBySubCategories(filters);
    }

    @GetMapping(value = "/categories/sub/shop/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void getShopStatsBySubCategoriesInShopGraph(
            HttpServletResponse response,

            @Valid @RequestBody ShopStatByCategoryFilter filters
    ) throws Exception {

        if (filters.getShopIds() == null || filters.getShopIds().size() != 1){
            throw new NotCorrectInput("ShopIds length must be 1");
        }

        List<ShopStatByCategoryDto> prices = priceService.getShopsStatBySubCategories(filters);

        if (prices.isEmpty()){
            throw  new NoDataForContentException("No prices were found to generate graph");
        }

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");

        List<CategoryStatDto> stats = prices.get(0).getCategories().stream()
                .sorted(Comparator.comparing(CategoryStatDto::getCategoryId)).toList();

        graphicService.generateBarForAverageCategoriesRate(
                response.getOutputStream(), "Sub categopries` prices in shop", "categories` ids",
                "prices", stats
        );

         response.getOutputStream().flush();
    }

    @GetMapping("/shops/districts")
    public List<DistrictStatisticDto> getShopStatsByDistricts(
            @Valid @RequestBody DistrictStatisticFilter filters
    ){
        return priceService.getShopsStatByDistricts(filters);
    }

    @GetMapping(value = "/shops/district/graph", produces = MediaType.IMAGE_PNG_VALUE)
    // либо теги, либо категории, но с тегами будут самые обширные категории
    public void getCategoriesStatsByDistrictGraph(
            HttpServletResponse response,
           @Valid @RequestBody DistrictStatisticFilter filters
            // если в фильтрах не стоит data range, о получают current prices
    ) throws Exception {

        if (filters.getDistrictIds() == null || filters.getDistrictIds().size() !=1){
            throw  new NotCorrectInput("categoryIds length must be 1");
        }


        List<DistrictStatisticDto> prices =  priceService.getShopsStatByDistricts(filters);
        if (prices.isEmpty()){
            throw  new NoDataForContentException("No prices were found to generate graph");
        }

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");

        graphicService.generateBarForAverageCategoriesInDistrict(
                response.getOutputStream(), "Categories' Average Prices in District " + filters.getDistrictIds().get(0),
                "categories", "prices", prices
        );
        response.getOutputStream().flush();

    }

    @GetMapping(value = "/shops/districts/category/graph", produces = MediaType.IMAGE_PNG_VALUE)
    // либо теги, либо категории, но с тегами будут самые обширные категории
    public void getCategoryStatsByDistrictsGraph(
            HttpServletResponse response,
            @Valid @RequestBody DistrictStatisticFilter filters
    ) throws Exception {

        if (filters.getCategoryIds() == null || filters.getCategoryIds().size() !=1){
            throw  new NotCorrectInput("categoryIds length must be 1");
        }



        List<DistrictStatisticDto> prices =  priceService.getShopsStatByDistricts(filters);
        if (prices.isEmpty()){
            throw  new NoDataForContentException("No prices were found to generate graph");
        }

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");
        prices = prices.stream().sorted(Comparator.comparing(DistrictStatisticDto::getDistrictId)).toList();

        graphicService.generateBarForAverageCategoryInDistricts(
                response.getOutputStream(), "Category`s " + filters.getCategoryIds().get(0) + "  Average Prices in District ",
                "districts", "prices", prices
        );
        response.getOutputStream().flush();

    }

    @GetMapping("/shops/{id}/statistics")
    public ShopStatisticDto getShopStatistics(@PathVariable("id") Long shopId){

        if (shopId <=0){
            throw new NotCorrectInput("path variable shop id  must be > 0");
        }
        return priceService.getShopStatistics(shopId);
    }

    @GetMapping("/shops/cart")
    public List<ShopCartDto> compareCartInShops(
            @Valid @RequestBody CartStatisticRequest request
            ){

        return priceService.compareCartByShops(request);
    }


    @GetMapping(value="/shops/cart/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void compareCartInShopsGraph(
            HttpServletResponse response,
            @Valid@RequestBody CartStatisticRequest request
    ) throws Exception {

        List<ShopCartDto> shops = priceService.compareCartByShops(request);

        if (shops.isEmpty()){
            throw  new NoDataForContentException("No prices were found to generate graph");
        }

        shops = shops.stream().sorted(Comparator.comparing(ShopCartDto::getShopId)).toList();
        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");

        graphicService.generateBarForCartPriceInShops(
                response.getOutputStream(),
                "Cart`s prices in shops", "Shops",
                "Prices",
                shops
        );
        response.getOutputStream().flush();

    }


}


