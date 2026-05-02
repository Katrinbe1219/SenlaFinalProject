package org.example.notification_service.hibernate;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.notification_service.models.Good;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GoodHib {
    private static final Logger logger = LogManager.getLogger(GoodHib.class);
    private final SessionFactory sessionFactory;

    @Transactional
    public List<Long> findGoodsBytTags(Long tagId){
        try{
            Session session = sessionFactory.getCurrentSession();
            return session.createNativeQuery("""
                SELECT good_id AS id FROM goods_tags WHERE tag_id = :tagId
                """, Long.class)
                    .setParameter("tagId", tagId)
                    .getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHib findGoodsBytTags " + e.getMessage());
            return List.of();

        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHib findGoodsBytTags: "+e.getMessage());
            return List.of();
        }
    }
}
