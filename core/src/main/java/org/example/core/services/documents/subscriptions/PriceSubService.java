package org.example.core.services.documents.subscriptions;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.PriceSubCreateDto;
import org.example.core.dto.getting.subscriptions.PriceSubGetDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.subscriptions.PriceSubFilter;
import org.example.core.hibernate.base_settings.service_dto.CheckForPriceSubscription;
import org.example.core.hibernate.documents.subscriptions.PriceSubHib;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.mapping.subscriptions.PriceSubGetMapper;
import org.example.core.models.Good;
import org.example.core.models.PriceSubscription;
import org.example.core.models.Shop;
import org.example.core.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PriceSubService {
    private static final Logger logger = LogManager.getLogger(PriceSubService.class);
    private PriceSubHib priceSubHib;
    private GoodHibImpl goodHib;
    private ShopHibImpl shopHib;
    private PriceSubGetMapper mapper;

    @Transactional
    public List<PriceSubGetDto> findAll(PriceSubFilter filters){
        try{
            List<PriceSubscription> prices = priceSubHib.findAll(filters);
            if (prices.isEmpty()) return List.of();

            List<PriceSubGetDto> dtos = new ArrayList<>();
            for(PriceSubscription price : prices){
                dtos.add(mapper.toDto(price));
            }
            return dtos;
        }catch (Exception e){
            logger.error("PriceSubService findAll: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public PriceSubGetDto createSubscription(PriceSubCreateDto dto, User user){
        try{


            CheckForPriceSubscription checking = priceSubHib.checking(dto.getShopId(), dto.getGoodId(), user.getId());
            if (checking == null || checking.getPrice() == null){
                throw new DoesNoeExist("Price does not exist with given credentials");
            }


            if( checking.getPrice().setScale(0, RoundingMode.DOWN).equals(dto.getPrice())){
                throw new NotCorrectInput("Price is already presented ");
            }
            if (checking.getPriceSubId() != null){
                throw new NotCorrectInput("Subscription is already presented ");
            }

            Shop shop = shopHib.getReferenceById(dto.getShopId());
            Good good = goodHib.getReferenceById(dto.getGoodId());



            PriceSubscription subscription = new PriceSubscription();
            subscription.setCreatedAt(Instant.now());
            subscription.setUser(user);
            subscription.setShop(shop);
            subscription.setGood(good);
            subscription.setTargetPrice(dto.getPrice());

            return mapper.toDto(priceSubHib.save(subscription, logger));
        }
        catch (NotCorrectInput | DoesNoeExist e){
            throw e;
        }
        catch(Exception e){
            logger.error("PriceSubService  createSubscription:" + e.getMessage());
            throw e;
        }
    }
}
