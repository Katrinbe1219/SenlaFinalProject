package org.example.core.hibernate.documents.subscriptions;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.subscriptions.PriceSubFilter;
import org.example.core.hibernate.base_settings.service_dto.CheckForPriceSubscription;
import org.example.core.models.PriceSubscription;
import org.example.core.models.Shop;
import org.example.core.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PriceSubHib extends HibernateAbstractDao<PriceSubscription, Long, Logger> {
    protected PriceSubHib() {
        super(PriceSubscription.class);
    }

    private static  final Logger logger = LogManager.getLogger(PriceSubHib.class);

    @Transactional
    public List<PriceSubscription> findAll(PriceSubFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<PriceSubscription> query = builder.createQuery(PriceSubscription.class);
            JpaRoot<PriceSubscription> root = query.from(PriceSubscription.class);

            List<JpaPredicate> predicates = buildPredicates(filters,builder,root);
            JpaOrder order = buildOrder(filters,builder,root);

            root.fetch("user", JoinType.LEFT);
            root.fetch("good", JoinType.LEFT);
            Fetch<PriceSubscription, Shop> shopFetch=root.fetch("shop", JoinType.LEFT);
            shopFetch.fetch("district", JoinType.LEFT);

            query.select(root)
                    .distinct(true)
                    .where(predicates.toArray(new JpaPredicate[predicates.size()]))
                    .orderBy(order);

            var cquery = session.createQuery(query);
            if (filters.getPage()!= null && filters.getSize()!=null){
                cquery.setFirstResult(filters.getPage()*filters.getSize())
                        .setMaxResults(filters.getSize());
            }
            return cquery.getResultList();

        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceSubHib findAll " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceSubHib findAll: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public CheckForPriceSubscription checking(Long shopId, Long goodId, Long userId){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createNativeQuery("""
    WITH shop_check AS (SELECT id FROM shops WHERE id = :shopId), 
         good_check AS (SELECT id FROM goods WHERE id = :goodId),
         price_check AS (SELECT id, price FROM prices WHERE good_id=:goodId AND shop_id=:shopId AND valid_to IS NULL),
         priceSub AS (SELECT id FROM price_subscriptions WHERE user_id=:userId AND good_id=:goodId AND shop_id=:shopId)
     SELECT (SELECT id FROM shop_check) AS shopId,
              (SELECT id FROM good_check) AS goodId,
              (SELECT id FROM price_check) AS priceId,
              (SELECT price FROM price_check) AS price,
    (SELECT id FROM priceSub ) AS priceSubId; 
""", CheckForPriceSubscription.class)
                    .setParameter("shopId", shopId)
                    .setParameter("goodId", goodId)
                    .setParameter("userId", userId)
                    .uniqueResultOptional().orElse(null);

        }catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceSubHib checking: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceSubHib checking: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }



    private List<JpaPredicate> buildPredicates(
            PriceSubFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<PriceSubscription> root
    ){

        List<JpaPredicate> predicates = new ArrayList<>();

        if (filters.getCurPrice()!=null){
            predicates.add(
                    builder.equal(root.get("targetPrice"), filters.getCurPrice())
            );
        }

        if (filters.getMinPrice() != null){
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("targetPrice"), filters.getMinPrice())
            );
        }

        if (filters.getMaxPrice() != null){
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("targetPrice"), filters.getMaxPrice())
            );
        }

        if (filters.getCurDate()!=null){
            predicates.add(
                    builder.between(root.get("createdAt"),
                            DateTimeUtils.toInstant(filters.getCurDate()),
                    DateTimeUtils.toInstantEndDay(filters.getCurDate())
            ));
        }

        if (filters.getStartDate()!=null){
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.get("createdAt"), DateTimeUtils.toInstant(filters.getStartDate())
                    )
            );
        }

        if (filters.getEndDate()!=null){
            predicates.add(
                    builder.lessThanOrEqualTo(
                            root.get("createdAt"),
                            DateTimeUtils.toInstantEndDay(filters.getEndDate())
                    )
            );
        }

        if (filters.getShopIds()!=null){
            predicates.add(
                    root.get("shop").get("id").in(filters.getShopIds())
            );
        }

        if (filters.getGoodIds()!=null){
            predicates.add(
                    root.get("good").get("id").in(filters.getGoodIds())
            );
        }

        if (filters.getUserIds()!=null){
            predicates.add(
                    root.get("user").get("id").in(filters.getUserIds())
            );
        }
        return predicates;
    }

    private JpaOrder buildOrder(
            PriceSubFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<PriceSubscription> root
    ){
        return switch(filters.getSortType()){
            case ASC -> builder.asc(root.get("id"));
            case DESC -> builder.desc(root.get("id"));
            case CREATED_AT_ASC -> builder.asc(root.get("createdAt"));
            case CREATED_AT_DESC -> builder.desc(root.get("createdAt"));
            case PRICE_ASC -> builder.asc(root.get("price"));
            case PRICE_DESC -> builder.desc(root.get("price"));
            case GOOD_ID_ASC -> builder.asc(root.get("good").get("id"));
            case GOOD_ID_DESC -> builder.desc(root.get("good").get("id"));
            case USER_ID_ASC -> builder.asc(root.get("user").get("id"));
            case USER_ID_DESC -> builder.desc(root.get("user").get("id"));
        };
    }


}
