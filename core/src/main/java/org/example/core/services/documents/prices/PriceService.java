package org.example.core.services.documents.prices;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.dto.getting.prices.*;
import org.example.core.dto.kafka.PriceChangedMessage;
import org.example.core.dto.kafka.PriceCreatedMessage;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.prices.PriceFilter;
import org.example.core.hibernate.base_settings.service_dto.CheckingPriceGoodShopExistence;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.documents.prices.PriceHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.models.Good;
import org.example.core.models.Price;
import org.example.core.models.Shop;
import org.example.core.services.documents.prices.data.GoodShopRecord;
import org.example.core.services.documents.prices.data.OptionForUpload;
import org.example.core.services.documents.prices.data.PriceCreateAllDto;
import org.example.core.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PriceService {
    private static final Logger logger = LogManager.getLogger(PriceService.class);

    PriceHibImpl priceHib;
    GoodHibImpl goodHib;
    ShopHibImpl shopHib;


    private ApplicationEventPublisher eventPublisher;

    private CategoryHibImpl categoryHibImpl;

    @Transactional
    public List<PriceGetDtoForUser> getAllForUser(Long goodId, Long shopId){
        try{
            return priceHib.getAllForUser(shopId, goodId);
        } catch (Exception e) {
            logger.error("PriceService getAllForUser: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
   public List<PriceGetDtoForUser> getComparison(PriceComparisonRequest request){
        try{
            return priceHib.compareByGoodAndShop(request);
        }catch (Exception e) {
            logger.error("PriceService getComparison: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public void deletePriceByGoodAndShop(Long goodId, Long shopId){
        // изменяется валидность, в истории сохраняется
        try{
            priceHib.makeInvalidPrice(goodId, shopId);
        }catch (Exception e) {
            logger.error("PriceService deletePriceByGoodAndShop: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public void deletePriceById(Long id){
        try{
            priceHib.delete(id, logger);
        }catch (Exception e) {
            logger.error("PriceService deletePriceById: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public PriceGetResultForModerator   getByIdForModerator(Long id){
        try{
            PriceGetResultForModerator result = priceHib.getByIdForModerator(id);
            if (result == null) {
                throw new DoesNoeExist("Price does not exist with given credentials");
            }
            return result;
        }catch (DoesNoeExist e){
            throw e;
        }
        catch (Exception e) {
            logger.error("PriceService getByIdForModerator: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<PriceGetResultForModerator> getByFilters(PriceFilter filters){
        try{
            Instant start = null;
            Instant end = null;

            if (filters.getStartDate() != null){
                start = DateTimeUtils.toInstant(filters.getStartDate());
            }

            if (filters.getEndDate() != null){
                end = DateTimeUtils.toInstant(filters.getEndDate());
            }

            if (filters.getCategoryIds()!= null){
                List<Long> allCat =  categoryHibImpl.findAllChildCategoryIds(filters.getCategoryIds());
                filters.setCategoryIds(allCat);
            }
            return priceHib.getPricesByFilter(filters, start, end);

        }catch(Exception e){
            logger.error("PriceService getByFilters: " + e.getMessage());
            throw e;
        }



    }



    @Transactional
    public PriceGetResultForModerator updatePrice(PriceCreateDto dto){
        try{
            CheckingPriceGoodShopExistence checking = priceHib.checkBeforeAddPrice(dto.getShopId(), dto.getGoodId());
            if (checking == null){
                throw new DoesNoeExist("Price does not exist with given credentials");
            }

            if ( checking.getPriceId() == null){
                throw new DoesNoeExist("Price does not exist with given credentials");
            }

            Good good = goodHib.getReferenceById(dto.getGoodId());
            Shop shop = shopHib.getReferenceById(dto.getShopId());


            Price price = new Price();
            price.setPrice(dto.getPrice());
            price.setGood(good);
            price.setShop(shop);
            price.setValidFrom(Instant.now());

            int num= priceHib.makeInvalidPrice(dto.getGoodId(), dto.getShopId());
            if (num != 0){
                eventPublisher.publishEvent(
                        new PriceChangedMessage(dto.getGoodId(),dto.getShopId(),  dto.getPrice())
                );
            }

            Price newPrice = priceHib.update(price, logger);
            return toDto(newPrice, good, shop);
        }catch (DoesNoeExist e){
            throw e;
        }
        catch (Exception e){
            logger.error("PriceService updatePrice: " + e.getMessage());
            throw e;
        }


    }

    @Transactional
    public PriceGetResultForModerator createPrice(PriceCreateDto dto){
        try{
            CheckingPriceGoodShopExistence checking = priceHib.checkBeforeAddPrice(dto.getShopId(), dto.getGoodId());
            if (checking == null){
                throw new NonHibernateException("PriceService createPrice checking-CheckingPriceGoodShopExistence is null");
            }

            if ( checking.getPriceId() != null){
                throw new NotCorrectInput("You can not create price because it  already exists");
            }
            if (checking.getShopId() == null){
                throw new NotCorrectInput("Shop does not exist with given credentials");
            }

            if (checking.getGoodId()  == null){
                throw new NotCorrectInput("Good does not exist with given credentials");
            }


            Good good = goodHib.getReferenceById(dto.getGoodId());
            Shop shop = shopHib.getReferenceById(dto.getShopId());


            Price price = new Price();
            price.setPrice(dto.getPrice());
            price.setGood(good);
            price.setShop(shop);
            price.setValidFrom(Instant.now());


            eventPublisher.publishEvent(
                    new PriceCreatedMessage(dto.getGoodId(), dto.getShopId(), dto.getPrice())
            );


            Price newPrice = priceHib.save(price, logger);
            return toDto(newPrice, good, shop);
        }catch (NotCorrectInput e){
            throw e;
        }
        catch (Exception e) {
            logger.error("PriceService createPrice: " + e.getMessage());
            throw e;
        }


    }

    @Transactional
    public void saveAll(List<PriceCreateDto> dtos, OptionForUpload option, boolean isSend){

        // при неверных данные отправится пользователю ошибка
        // option -> skip: получаем только те, которые изменились -> нельзя отправлять ничего - требование
        // option -> stop: все новые, нет никаких проблем, можно использовать dtos ля отправки новых цен, ошибка вызовется при конфликте
        // replace -> как новые могут быть, так и замены
        try{
            Map<GoodShopRecord, BigDecimal> oldValues = null;
            if (option == OptionForUpload.REPLACE && isSend) {
                List<Long> goodIds = dtos.stream().map(PriceCreateDto::getGoodId).toList();
                List<Long> shopIds = dtos.stream().map(PriceCreateDto::getShopId).toList();
                List<Object[]> invalids = priceHib.makeInvalidManyWithReturning(goodIds, shopIds);

                oldValues = invalids.stream().collect(Collectors.toMap(
                        d-> new GoodShopRecord((Long) d[0], (Long) d[1]),
                        d -> (BigDecimal) d[3]
                ));
            }

            priceHib.saveAll(dtos, option);
            if (option == OptionForUpload.STOP && isSend){
                for (PriceCreateDto dto : dtos){
                    eventPublisher.publishEvent(
                            new PriceCreatedMessage(dto.getGoodId(), dto.getShopId(), dto.getPrice())
                    );
                }

            }

            else if (option == OptionForUpload.REPLACE && isSend){
                for (PriceCreateDto dto : dtos){
                    BigDecimal oldPrice = oldValues.get(new GoodShopRecord(dto.getGoodId(), dto.getShopId()));
                    if (oldPrice != null){
                        eventPublisher.publishEvent(
                                new PriceChangedMessage(dto.getGoodId(), dto.getShopId(), dto.getPrice())
                        );
                    }else{
                        eventPublisher.publishEvent(
                                new PriceCreatedMessage(dto.getGoodId(), dto.getShopId(), dto.getPrice())
                        );
                    }
                }
            }
        }catch (Exception e){
            logger.error("PriceService saveAll: " + e.getMessage());
            throw e;
        }

    }



    private PriceGetResultForModerator toDto(Price old, Good good, Shop shop){

        try{
            PriceGetResultForModerator dto = new PriceGetResultForModerator();
            dto.setId(old.getId());
            dto.setPrice(old.getPrice());
            dto.setValidFrom(old.getValidFrom());

            dto.setValidTo(old.getValidTo());

            dto.setShopId(shop.getId());
            dto.setAddress(shop.getAddress());
            dto.setShopName(shop.getName());

            dto.setGoodId(good.getId());
            dto.setGoodName(good.getName());
            return dto;
        }catch (Exception e){
            logger.error("PriceService toDto: " + e.getMessage());
            throw e;
        }

    }
}
