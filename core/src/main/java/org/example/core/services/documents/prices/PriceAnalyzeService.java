package org.example.core.services.documents.prices;

import org.example.core.dto.getting.goods.GoodAnalyseForShopDto;
import org.example.core.dto.getting.goods.GoodPriceInShop;
import org.example.core.dto.getting.prices.PriceInTime;
import org.example.core.dto.getting.statistics.CartStatisticRequest;
import org.example.core.dto.getting.statistics.DistrictStatisticDto;
import org.example.core.dto.getting.statistics.shops.ShopCartDto;
import org.example.core.dto.getting.statistics.shops.ShopStatByCategoryDto;
import org.example.core.dto.getting.statistics.shops.ShopStatisticDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.goods.GoodPriceInShopsFilter;
import org.example.core.hibernate.base_settings.filters.prices.DistrictStatisticFilter;
import org.example.core.hibernate.base_settings.filters.prices.PriceInTimeFilter;
import org.example.core.hibernate.base_settings.filters.prices.ShopStatByCategoryFilter;
import org.example.core.hibernate.documents.prices.PriceAnalyseHibImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PriceAnalyzeService {
    private PriceAnalyseHibImpl priceHib;
    public PriceAnalyzeService(PriceAnalyseHibImpl priceAnalyseHib) {
        this.priceHib = priceAnalyseHib;
    }

    @Transactional
    public List<ShopStatByCategoryDto> getShopsStatByMainCategories(ShopStatByCategoryFilter filters){
        return priceHib.getShopsStatisticsByMainCategories(filters);
    }
    @Transactional
    public List<ShopStatByCategoryDto> getShopsStatBySubCategories(ShopStatByCategoryFilter filters){
        return priceHib.getShopsStatisticsBySubCategories(filters);
    }

    @Transactional
    public List<DistrictStatisticDto> getShopsStatByDistricts(DistrictStatisticFilter filters){
        if(filters.getCategoriesId() != null && filters.getGoodsId() != null && filters.getTagsIds() != null){
            throw new NotCorrectInput("Either categories or goods or tags");
        }

        if ((filters.getCategoriesId() != null && filters.getGoodsId() != null) ||
                (filters.getCategoriesId() != null && filters.getTagsIds() != null) ||
                (filters.getGoodsId() != null && filters.getTagsIds() != null)
        ){
            throw new NotCorrectInput("Either categories or goods or tags");
        }
        return priceHib.getDistrictStatistic(filters);
    }

    @Transactional
    public List<GoodAnalyseForShopDto> getGoodsByShop(String type, Long shopId, int count){

        if (type.equals("max")){
            return priceHib.getExpensiveGoodsByShop(shopId, count);
        }else{
            return priceHib.getCheapestGoodsByShop(shopId, count);
        }
    }

    @Transactional
    public List<GoodPriceInShop> getGoodPricesInShops(GoodPriceInShopsFilter filters){
        return priceHib.getGoodPricesInShops(filters);
    }

    @Transactional
    public List<PriceInTime> getGoodPriceInTime(PriceInTimeFilter filters){
        return priceHib.getGoodPriceInTime(filters);
    }

    @Transactional
    public ShopStatisticDto getShopStatistics(Long shopId){
        return priceHib.getShopStatistic(shopId);
    }

    @Transactional
    public List<ShopCartDto> compareCartByShops(CartStatisticRequest request){
        return priceHib.compareCartInShops(request);
    }
}
