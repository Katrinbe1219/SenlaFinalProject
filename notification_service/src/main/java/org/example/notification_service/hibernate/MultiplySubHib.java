package org.example.notification_service.hibernate;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.notification_service.models.Category;
import org.example.notification_service.models.Good;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MultiplySubHib {
    private final static Logger logger = LogManager.getLogger(MultiplySubHib.class);
    private final SessionFactory sessionFactory;

    @Transactional
    public List<String> getUserEmails (List<Long> goodIds, List<Long> catIds, Long shopId){
        try{
            Session session = sessionFactory.getCurrentSession();

            StringBuilder priceSQl = new StringBuilder("");
            StringBuilder avalSQl = new StringBuilder("");
            priceSQl.append("SELECT DISTINCT u.email \n" +
                    "FROM price_subscriptions AS price_sub ");
            avalSQl.append(" UNION ALL  SELECT DISTINCT u.email FROM availability_subscriptions  AS a ");

            // build joins

            if ((goodIds != null && !goodIds.isEmpty()) ||  (catIds != null && !catIds.isEmpty()) ){
                priceSQl.append(" INNER JOIN goods AS g ON price_sub.good_id = g.id  ");
                avalSQl.append(" INNER JOIN goods AS g ON a.good_id = g.id  ");

            }
            if (catIds != null && !catIds.isEmpty()) {
                priceSQl.append(" INNER JOIN categories AS c ON g.category_id = c.id ");
                avalSQl.append(" INNER JOIN categories AS c ON g.category_id = c.id ");
            }

            priceSQl.append(" INNER JOIN users AS u ON price_sub.user_id = u.id WHERE u.email IS NOT NULL    ");
            avalSQl.append(" INNER JOIN users  AS u ON a.user_id = u.id  WHERE u.email IS NOT NULL    ");

            // build WHERE
            boolean prev = false;

            if (shopId != null) {
                prev = true;
                priceSQl.append(" AND  ( price_sub.shop_id =  :shopId ");
                avalSQl.append(" AND ( a.shop_id =  :shopId ");
            }
            if (goodIds != null && !goodIds.isEmpty()) {
                if (prev) {
                    priceSQl.append(" OR ");
                    avalSQl.append(" OR ");
                }else{
                    avalSQl.append(" AND ( ");
                    priceSQl.append(" AND (  ");
                }
                prev = true;
                priceSQl.append("  price_sub.good_id IN (:goodIds) ");
                avalSQl.append("  a.good_id IN (:goodIds) ");
            }
            if (catIds != null && !catIds.isEmpty()) {
                if (prev) {
                    priceSQl.append(" OR ");
                    avalSQl.append(" OR ");
                }else{
                    avalSQl.append(" AND ( ");
                    priceSQl.append(" AND ( ");
                }
                prev = true;
                priceSQl.append("  g.category_id IN (:catIds) ");
                avalSQl.append("  g.category_id IN (:catIds) ");

            }

            if (prev){
                avalSQl.append(" ) ");
                priceSQl.append(" ) ");
            }




            priceSQl.append(avalSQl);

            var query =session.createNativeQuery(priceSQl.toString(), String.class);

            if (shopId != null) {
                query.setParameter("shopId", shopId);
            }
            if (goodIds != null && !goodIds.isEmpty()) {
                query.setParameterList("goodIds", goodIds);
            }
            if (catIds != null && !catIds.isEmpty()) {
                query.setParameterList("catIds", catIds);
            }

            return query.getResultList();


        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в MultiplySubHib getUserIds " + e.getMessage());
            return List.of();

        }
        catch (Exception e){
            logger.error("NonHibernate Exception MultiplySubHib getUserIds: "+e.getMessage());
            return List.of();
        }
    }


}
