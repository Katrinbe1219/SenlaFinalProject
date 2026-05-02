package org.example.notification_service.hibernate;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.notification_service.models.Category;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@AllArgsConstructor
public class CategoryHib {
    private static final Logger logger = LogManager.getLogger(CategoryHib.class);
    private SessionFactory sessionFactory;

    @Transactional
    public List<Category> getCategories(Long id){
        try{
            Session session = sessionFactory.getCurrentSession();
            return session.createNativeQuery("""
        WITH RECURSIVE main_category AS (
            SELECT id FROM categories AS c WHERE c.id = :cId AND parent_id IS NULL
                                   UNION ALL
                           SELECT c.id FROM categories AS c
                               INNER JOIN main_category AS ct ON ct.id = c.parent_id
        )       
            SELECT distinct id FROM main_category
        """, Category.class).setParameter("cId", id).getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в CategoryHib getCategories " + e.getMessage());
            return List.of();

        }
        catch (Exception e){
            logger.error("NonHibernate Exception CategoryHib getCategories: "+e.getMessage());
            return List.of();
        }
    }
}
