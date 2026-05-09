package org.example.core.services.documents.subscriptions;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.subscriptions.AvailabilitySubGetDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.CheckingMultiExistenceHib;
import org.example.core.hibernate.base_settings.filters.subscriptions.AvailabilitySubFilter;
import org.example.core.hibernate.documents.subscriptions.AvailabilitySubHib;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.mapping.subscriptions.AvailabilitySubGetMapper;
import org.example.core.models.AvailabilitySubscription;
import org.example.core.models.Good;
import org.example.core.models.Shop;
import org.example.core.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AvailabilitySubService {
    private static final Logger logger = LogManager.getLogger(AvailabilitySubService.class);

    private AvailabilitySubHib availabilitySubHib;
    private AvailabilitySubGetMapper mapper;
    private GoodHibImpl goodHibImpl;
    private ShopHibImpl shopHibImpl;
    private CheckingMultiExistenceHib checkHib;


    @Transactional
    public List<AvailabilitySubGetDto> findAll(AvailabilitySubFilter filters){
        try{
            List<AvailabilitySubscription> subs = availabilitySubHib.findAll(filters);
            if (subs.isEmpty()) return List.of();

            List<AvailabilitySubGetDto> dtos = new ArrayList<>();
            for (AvailabilitySubscription sub : subs) {
                dtos.add(mapper.toDto(sub));
            }
            return dtos;
        }
        catch(Exception e){
            logger.error("AvailabilitySubService  findAll:" + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public AvailabilitySubGetDto createSubscription(User user, Long goodId, Long shopId){
        try{

            Map<String, Boolean> checking = checkHib.checkShopAndGoodByIds(goodId, shopId, user.getId());
            if (!checking.getOrDefault("goodId", false)){
                throw new DoesNoeExist("Good does not exist with given credentials");
            }

            if (!checking.getOrDefault("shopId", false)){
                throw new DoesNoeExist("Shop does not exist with given credentials");
            }

            if (checking.getOrDefault("priceId", false)){
                throw new NotCorrectInput("Good exists in current shop");
            }

            if (checking.getOrDefault("sub", false)){
                throw new NotCorrectInput("Subscription already exists");
            }

            Shop shop = shopHibImpl.getReferenceById(shopId);
            Good good = goodHibImpl.getReferenceById(goodId);

            AvailabilitySubscription sub = new AvailabilitySubscription();
            sub.setUser(user);
            sub.setShop(shop);
            sub.setGood(good);
            sub.setCreatedAt(Instant.now());
            return mapper.toDto(availabilitySubHib.save(sub, logger));

        }
        catch (NotCorrectInput | DoesNoeExist e){
            throw e;
        }
        catch(Exception e){
            logger.error("AvailabilitySubService  createSubscription:" + e.getMessage());
            throw e;
        }

    }
}
