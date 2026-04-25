package org.example.core.services.documents.prices;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.dto.getting.prices.*;
import org.example.core.dto.kafka.PriceChangedMessage;
import org.example.core.dto.kafka.PriceCreatedMessage;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.prices.PriceFilter;
import org.example.core.hibernate.base_settings.service_dto.CheckingPriceGoodShopExistence;
import org.example.core.hibernate.documents.prices.PriceHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.models.Good;
import org.example.core.models.Price;
import org.example.core.models.Shop;
import org.example.core.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class PriceService {
    private static final Logger logger = LogManager.getLogger(PriceService.class);

    PriceHibImpl priceHib;
    GoodHibImpl goodHib;
    ShopHibImpl shopHib;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
    public PriceGetResultForModerator updatePrice(PriceCreateDto dto){

        CheckingPriceGoodShopExistence checking = priceHib.checkBeforeAddPrice(dto.getShopId(), dto.getGoodId());
        if (checking == null){
            throw new DoesNoeExist("Price does not exist with given credentials");
        }

        Good good = goodHib.getReferenceById(dto.getGoodId());
        Shop shop = shopHib.getReferenceById(dto.getShopId());


        Price price = new Price();
        price.setPrice(dto.getPrice());
        price.setGood(good);
        price.setShop(shop);
        price.setValidFrom(Instant.now());

        Integer num= priceHib.makeInvalidPrice(dto.getGoodId(), dto.getShopId());
        if (num != 0){
            eventPublisher.publishEvent(
                    new PriceChangedMessage(dto.getShopId(), dto.getGoodId(), dto.getPrice())
            );
        }

        Price newPrice = priceHib.update(price, logger);
        return toDto(newPrice, good, shop);

    }

    @Transactional
    public PriceGetResultForModerator createPrice(PriceCreateDto dto){

        CheckingPriceGoodShopExistence checking = priceHib.checkBeforeAddPrice(dto.getShopId(), dto.getGoodId());
        if (checking != null){
            throw new NotCorrectInput("You can not create price because it  already exists");
        }

        Good good = goodHib.getReferenceById(dto.getGoodId());
        Shop shop = shopHib.getReferenceById(dto.getShopId());


        Price price = new Price();
        price.setPrice(dto.getPrice());
        price.setGood(good);
        price.setShop(shop);
        price.setValidFrom(Instant.now());


        eventPublisher.publishEvent(
                    new PriceCreatedMessage(dto.getShopId(), dto.getGoodId(), dto.getPrice())
        );


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
