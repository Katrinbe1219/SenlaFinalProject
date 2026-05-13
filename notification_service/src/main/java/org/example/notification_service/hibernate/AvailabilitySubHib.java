package org.example.notification_service.hibernate;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.notification_service.models.AvailabilitySubscription;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@AllArgsConstructor
public class AvailabilitySubHib {
    private static final Logger logger = LogManager.getLogger(AvailabilitySubHib.class);

    private SessionFactory sessionFactory;


    @Transactional
    public List<AvailabilitySubscription> findAll(Long goodId, Long shopId){
        try{
            Session session =sessionFactory.getCurrentSession();
            return session.createQuery("SELECT a from AvailabilitySubscription a " +
                            "LEFT JOIN FETCH a.user" +
                            " LEFT JOIN FETCH a.shop " +
                            "LEFT JOIN FETCH a.good " +
                            "WHERE a.good.id = :goodId AND a.shop.id = :shopId AND a.user.email IS NOT NULL",
                    AvailabilitySubscription.class)
                    .setParameter("goodId", goodId)
                    .setParameter("shopId", shopId).getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в AvailabilitySubHib findAll " + e.getMessage());
            return List.of();

        }
        catch (Exception e){
            logger.error("NonHibernate Exception AvailabilitySubHib findAll: "+e.getMessage());
            return List.of();
        }
    }

    @Transactional
    public void deleteByIds(List<Long> ids){
        try{
            Session session =sessionFactory.getCurrentSession();
            session.createMutationQuery("DELETE FROM AvailabilitySubscription a  WHERE id in (:ids)")
                    .setParameter("ids", ids).executeUpdate();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в AvailabilitySubHib deleteByIds " + e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception AvailabilitySubHib deleteByIds: "+e.getMessage());

        }
    }
}
