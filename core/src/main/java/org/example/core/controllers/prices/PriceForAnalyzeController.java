package org.example.core.controllers.prices;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/prices/analyst")
//TODO Exception went to JwtFilter and causes Not authorize!!!!!!!
public class PriceForAnalyzeController {

    private PriceAnalyzeService priceService;
    private GraphicalAnalyseService graphicService;

    public PriceForAnalyzeController(PriceAnalyzeService priceService, GraphicalAnalyseService graphicService) {
        this.priceService = priceService;
        this.graphicService = graphicService;

    }

    @GetMapping("/shops/{id}/goods")
    // получение максимальных и минимальных продуктов
    public List<GoodAnalyseForShopDto> getGoodsByShopId(
            @PathVariable("id") Long id,
            @RequestParam(value = "type", defaultValue = "max", required = false) String type,
            @RequestParam(value = "count", defaultValue = "4", required = false) int count
    ) {
        if (count <=0){
            throw new NotCorrectInput("Count must be greater than 0");
        }

        if (!type.equals("max") && !type.equals("min")) {
            throw new NotCorrectInput("Type param must be min or max");
        }
        return priceService.getGoodsByShop(type, id, count);

    }

    @GetMapping(value = "/shop/goods/{id}/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void getGoodPricesInShops(
            HttpServletResponse response,
            @PathVariable("id") Long goodId,
            @Valid @RequestBody GoodPriceInShopsFilter filters
    ) throws Exception {

        filters.setGoodId(goodId);
        List<GoodPriceInShop> prices = priceService.getGoodPricesInShops(filters);

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");


        graphicService.generateBarForGoodPriceInShops(
                response.getOutputStream(), "Good Price in shops", "shops", "prices", prices
        );

        response.getOutputStream().flush();

    }

    @GetMapping(value = "/shops/{shopId}/goods/{id}/graph", produces = MediaType.IMAGE_PNG_VALUE)
    // получение максимальных и минимальных продуктов
    public void getGoodsByShopId(
            HttpServletResponse response,
            @Valid @RequestBody PriceInTimeFilter filters,
            @PathVariable("id") Long goodId,
            @PathVariable("shopId") Long shopId
    ) throws Exception {

        filters.setShopId(shopId);

        filters.setGoodId(goodId);


        List<PriceInTime> prices =  priceService.getGoodPriceInTime(filters);
        if (prices.isEmpty()){
            throw new DoesNoeExist("Nothing was found");
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

    @GetMapping(value = "/categories/main/shops/{id}/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void getShopsStatsByMainShopIdGraph(
            HttpServletResponse response,
            @PathVariable("id") Long shopId,
            @Valid @RequestBody ShopStatByCategoryFilter filters
    ) throws Exception {
        filters.setShopIds(List.of(shopId));


        List<ShopStatByCategoryDto> categories = priceService.getShopsStatByMainCategories(filters);
        if (categories.isEmpty()){
            throw new DoesNoeExist("Nothing was found with given credentials");
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

    @GetMapping(value = "/categories/sub/shops/{id}/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void getShopsStatsBySubCategoriesInShopGraph(
            HttpServletResponse response,
            @PathVariable("id") Long shopId,
            @Valid @RequestBody ShopStatByCategoryFilter filters
    ) throws Exception {

        filters.setShopIds(List.of(shopId));


        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");
        List<ShopStatByCategoryDto> prices = priceService.getShopsStatBySubCategories(filters);

        if (prices.isEmpty()){
            throw new DoesNoeExist("Nothing was found with given credentials");
        }
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

    @GetMapping(value = "/shops/districts/{id}/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void getCategoriesStatsByDistrictGraph(
            HttpServletResponse response,
            @PathVariable("id") Long districtId,
           @Valid @RequestBody DistrictStatisticFilter filters
    ) throws Exception {
        filters.setDistrictsId(List.of(districtId));
        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");

        List<DistrictStatisticDto> prices =  priceService.getShopsStatByDistricts(filters);
        if (prices.isEmpty()){
            throw new DoesNoeExist("Nothing was found with given credentials");
        }

        graphicService.generateBarForAverageCategoriesInDistrict(
                response.getOutputStream(), "Categories' Average Prices in District " + districtId,
                "categories", "prices", prices
        );
        response.getOutputStream().flush();

    }

    @GetMapping(value = "/shops/districts/categories/{id}/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void getCategoryStatsByDistrictsGraph(
            HttpServletResponse response,
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody DistrictStatisticFilter filters
    ) throws Exception {
        filters.setCategoriesId(List.of(categoryId));

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");

        List<DistrictStatisticDto> prices =  priceService.getShopsStatByDistricts(filters);
        if (prices.isEmpty()){
            throw new DoesNoeExist("Nothing was found with given credentials");
        }
        prices = prices.stream().sorted(Comparator.comparing(DistrictStatisticDto::getDistrictId)).toList();

        graphicService.generateBarForAverageCategoryInDistricts(
                response.getOutputStream(), "Category`s " + categoryId + "  Average Prices in District ",
                "districts", "prices", prices
        );
        response.getOutputStream().flush();

    }

    @GetMapping("/shops/{id}/statistics")
    public ShopStatisticDto getShopStatistics(@PathVariable("id") Long shopId){
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
            throw new DoesNoeExist("Nothing was found");
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


