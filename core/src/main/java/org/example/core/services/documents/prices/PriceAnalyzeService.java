package org.example.core.services.documents.prices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private  static final Logger logger = LogManager.getLogger(PriceAnalyzeService.class);
    private PriceAnalyseHibImpl priceHib;
    public PriceAnalyzeService(PriceAnalyseHibImpl priceAnalyseHib) {
        this.priceHib = priceAnalyseHib;
    }

    @Transactional
    public List<ShopStatByCategoryDto> getShopsStatByMainCategories(ShopStatByCategoryFilter filters){
        try{
            return priceHib.getShopsStatisticsByMainCategories(filters);
        }catch (Exception e){
            logger.error("PriceAnalyzeService getShopsStatByMainCategories" + e.getMessage());
            throw e;
        }

    }
    @Transactional
    public List<ShopStatByCategoryDto> getShopsStatBySubCategories(ShopStatByCategoryFilter filters){
        try{
            return priceHib.getShopsStatisticsBySubCategories(filters);
        }catch (Exception e){
            logger.error("PriceAnalyzeService getShopsStatBySubCategories" + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<DistrictStatisticDto> getShopsStatByDistricts(DistrictStatisticFilter filters){
        try{
            return priceHib.getDistrictStatistic(filters);
        }catch (Exception e){
            logger.error("PriceAnalyzeService getShopsStatByDistricts" + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<GoodAnalyseForShopDto> getGoodsByShop(String type, Long shopId, int count){
        try{
            if (type.equals("max")){
                return priceHib.getExpensiveGoodsByShop(shopId, count);
            }else{
                return priceHib.getCheapestGoodsByShop(shopId, count);
            }
        }catch (Exception e){
            logger.error("PriceAnalyzeService getGoodsByShop" + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<GoodPriceInShop> getGoodPricesInShops(GoodPriceInShopsFilter filters){
        try{
            return priceHib.getGoodPricesInShops(filters);
        } catch (RuntimeException e) {
            logger.error("PriceAnalyzeService getGoodPricesInShops" + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<PriceInTime> getGoodPriceInTime(PriceInTimeFilter filters){
        try{
            return priceHib.getGoodPriceInTime(filters);
        }
        catch (Exception e){
            logger.error("PriceAnalyzeService getGoodPriceInTime" + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public ShopStatisticDto getShopStatistics(Long shopId){
        try{
            return priceHib.getShopStatistic(shopId);
        }catch (Exception e){
            logger.error("PriceAnalyzeService getShopStatistics " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<ShopCartDto> compareCartByShops(CartStatisticRequest request){
        try{
            return priceHib.compareCartInShops(request);
        }catch (Exception e){
            logger.error("PriceAnalyzeService compareCartByShops " + e.getMessage());
            throw e;
        }

    }
}
