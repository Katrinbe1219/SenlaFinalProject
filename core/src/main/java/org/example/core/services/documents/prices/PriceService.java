package org.example.core.services.documents.prices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.dto.getting.prices.*;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.hibernate.base_settings.filters.prices.PriceFilter;
import org.example.core.hibernate.documents.prices.PriceHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.models.Good;
import org.example.core.models.Price;
import org.example.core.models.Shop;
import org.example.core.utils.DateTimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PriceService {
    private static final Logger logger = LogManager.getLogger(PriceService.class);

    PriceHibImpl priceHib;
    GoodHibImpl goodHib;
    ShopHibImpl shopHib;

    public PriceService(PriceHibImpl priceHib, GoodHibImpl goodHib, ShopHibImpl shopHib) {
        this.priceHib = priceHib;
        this.goodHib = goodHib;
        this.shopHib = shopHib;
    }

    @Transactional
    public List<PriceGetDtoForUser> getAllForUser(Long goodId, Long shopId, int count, int page){
        return priceHib.getAllForUser(shopId, goodId, count, page);
    }

    @Transactional
   public List<PriceGetDtoForUser> getComparison(PriceComparisonRequest request){
        return priceHib.compareByGoodAndShop(request);
    }

    @Transactional
    public void deletePriceByGoodAndShop(Long goodId, Long shopId){
        // изменяется валидность, в истории сохраняется
        priceHib.makeInvalidPrice(goodId, shopId);
    }

    @Transactional
    public void deletePriceById(Long id){
        priceHib.delete(id, logger);
    }

    @Transactional
    public PriceGetResultForModerator   getByIdForModerator(Long id){
        PriceGetResultForModerator result = priceHib.getByIdForModerator(id);
        if (result == null) {
            throw new DoesNoeExist("Price does not exist with given credentials");
        }
        return result;
    }

    @Transactional
    public List<PriceGetResultForModerator> getByFilters(PriceFilter filters){
        Instant start = null;
        Instant end = null;

        if (filters.getMinDate() != null){
            start = DateTimeUtils.toInstant(filters.getMinDate());
        }

        if (filters.getMaxDate() != null){
            end = DateTimeUtils.toInstant(filters.getMaxDate());
        }
        return priceHib.getPricesByFilter(filters, start, end);


    }



    @Transactional
    public PriceGetResultForModerator createPrice(PriceCreateDto dto){
        Good good = goodHib.findById(dto.getGoodId(), logger);
        if (good == null){
            throw new DoesNoeExist("Good does not exist with given credentials");
        }

        Shop shop = shopHib.findById(dto.getShopId(), logger);
        if (shop == null){
            throw new DoesNoeExist("Shop does not exist with given credentials");
        }

        Price price = new Price();
        price.setPrice(dto.getPrice());
        price.setGood(good);
        price.setShop(shop);
        price.setValidFrom(Instant.now());

        priceHib.makeInvalidPrice(dto.getGoodId(), dto.getShopId());

        Price newPrice = priceHib.save(price, logger);
        return toDto(newPrice, good, shop);

    }




    private PriceGetResultForModerator toDto(Price old, Good good, Shop shop){
        PriceGetResultForModerator dto = new PriceGetResultForModerator();
        dto.setPrice(old.getPrice());
        dto.setValidFrom(old.getValidFrom());
        //if(dto.setValidFrom() !)
        //TODO if null does it work?
        dto.setValidTo(old.getValidTo());

        dto.setShopId(shop.getId());
        dto.setAddress(shop.getAddress());
        dto.setShopName(shop.getName());

        dto.setGoodId(good.getId());
        dto.setGoodName(good.getName());
        return dto;
    }
}
