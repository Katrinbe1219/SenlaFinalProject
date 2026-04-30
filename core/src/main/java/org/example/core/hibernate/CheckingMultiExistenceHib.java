package org.example.core.hibernate;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Repository
@AllArgsConstructor
public class CheckingMultiExistenceHib {
    private static final Logger logger = LogManager.getLogger(CheckingMultiExistenceHib.class);

    private SessionFactory sessionFactory;

    @Transactional
    public Map<String,Boolean> checkShopAndGoodByIds(Long goodId, Long shopId, Long userId){
        try{
            Session session = sessionFactory.getCurrentSession();
            Tuple tuple = session.createNativeQuery("""
WITH shop_check AS (SELECT id FROM shops WHERE id = :shopId), 
         good_check AS (SELECT id FROM goods WHERE id = :goodId),
         price_existence AS (SELECT id FROM prices WHERE good_id=:goodId AND shop_id= :shopId AND valid_to IS NULL),
         subscription_existnce AS (SELECT id FROM availability_subscriptions WHERE good_id=:goodId AND shop_id= :shopId AND user_id=:userId)
     SELECT (SELECT id FROM shop_check) AS shopId,
              (SELECT id FROM good_check) AS goodId,
              (SELECT id FROM price_existence) AS priceId,
                (SELECT id FROM subscription_existnce) AS sub; 
""", Tuple.class)
                    .setParameter("shopId", shopId)
                    .setParameter("goodId", goodId)
                    .setParameter("userId", userId)
                    .uniqueResultOptional().orElse(null);

            return Map.of("goodId", tuple.get("goodId", Long.class) != null,
                    "shopId", tuple.get("shopId", Long.class) != null,
                    "priceId", tuple.get("priceId", Long.class) != null,
                    "sub", tuple.get("sub", Long.class) != null);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в CheckingMultiExistenceHib checkShopAndGoodByIds " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception CheckingMultiExistenceHib checkShopAndGoodByIds: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }
}
