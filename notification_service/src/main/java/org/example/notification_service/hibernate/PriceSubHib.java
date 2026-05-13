package org.example.notification_service.hibernate;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.notification_service.dto.GoodShopPair;
import org.example.notification_service.models.AvailabilitySubscription;
import org.example.notification_service.models.PriceSubscription;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
@AllArgsConstructor
public class PriceSubHib {
    private static  final Logger logger = LogManager.getLogger(PriceSubHib.class);
    private SessionFactory sessionFactory;

    @Transactional
    public List<PriceSubscription> findAll(Long goodId, Long shopId, BigDecimal price){
        try{
            Session session =sessionFactory.getCurrentSession();

            return session.createQuery("SELECT a from PriceSubscription a " +
                                    "LEFT JOIN FETCH a.user" +
                                    " LEFT JOIN FETCH a.shop " +
                                    "LEFT JOIN FETCH a.good " +
                                    "WHERE a.good.id = :goodId AND a.targetPrice >= : price AND a.user.email IS NOT NULL" +
                                    " AND (a.shop.id = :shopId  OR a.shop IS NULL )",
                    PriceSubscription.class)
                    .setParameter("shopId", shopId)
                    .setParameter("goodId", goodId)
                    .setParameter("price", price).getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в AvailabilitySubHib remove " + e.getMessage());
            return List.of();
        }
        catch (Exception e){
            logger.error("NonHibernate Exception AvailabilitySubHib remove: "+e.getMessage());
            return List.of();
        }

    }

    @Transactional
    public List<PriceSubscription> findAllByPairs(List<GoodShopPair> pairs){
        try{
            Session session =sessionFactory.getCurrentSession();
            Long[] goodIds= pairs.stream().map(GoodShopPair::goodId).toArray(Long[]::new);
            Long[]shopIds = pairs.stream().map(GoodShopPair::shopId).toArray(Long[]::new);
            List<Long> ids = session.createNativeQuery(
                    "SELECT ps.id FROM price_subscriptions ps " +
                            "JOIN unnest(:goodIds, :shopIds) AS pairs(goodId,shopId) " +
                            "ON ps.good_id = pairs.goodId AND ps.shop_id = pairs.shopId ",
                            Long.class)
                    .setParameter("goodIds", goodIds)
                    .setParameter("shopIds", shopIds)
                    .getResultList();

            if (ids.isEmpty()) return List.of();
            return session.createQuery(
                            "SELECT ps FROM PriceSubscription ps " +
                                    "JOIN FETCH ps.user " +
                                    "JOIN FETCH ps.good " +
                                    "JOIN FETCH ps.shop " +
                                    "WHERE ps.id IN :ids",
                            PriceSubscription.class)
                    .setParameter("ids", ids)
                    .getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в AvailabilitySubHib findAllByPairs " + e.getMessage());
            return List.of();
        }
        catch (Exception e){
            logger.error("NonHibernate Exception AvailabilitySubHib findAllByPairs: "+e.getMessage());
            return List.of();
        }

    }
}
