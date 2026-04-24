package org.example.core.hibernate.objects;

import jakarta.persistence.criteria.JoinType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.models.Shop;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@DependsOn("liquibase")
@Repository
public class ShopHibImpl extends HibernateAbstractDao<Shop, Long, Logger> {

    private final static Logger logger = LogManager.getLogger(ShopHibImpl.class);
    ShopHibImpl() {
        super(Shop.class);
    }

    @Transactional
    public List<Shop> findAllFullVersion(Integer count, Integer page) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Shop> query = builder.createQuery(Shop.class);
            JpaRoot<Shop> root = query.from(Shop.class);
            root.fetch("district", JoinType.LEFT);
            query.select(root);
            // здесь пагинация работает нормально, так как связь manyToOne
            // - не подгружается куча всего из-за чего пагинация может сбиться
            var sQuery = session.createQuery(query);
            if (count != null && page != null){
                sQuery.setFirstResult(count*page)
                        .setMaxResults(count);
            }

            return sQuery.getResultList();
        }catch(HibernateException e){
            logger.error("Hibenate ShopHibimpl findallFullVersion: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ShopHibimpl findallFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public Shop findByIdFullVersion(Long id){
        Session session = getSessionFactory().getCurrentSession();
        try{
            Optional<Shop> shop = session.createQuery("""
                SELECT s FROM Shop s
                LEFT JOIN FETCH s.district district
                WHERE s.id = :id
                """, Shop.class)
                    .setParameter("id", id).uniqueResultOptional();
            return shop.orElse(null);
        }
        catch(HibernateException e){
            logger.error("Hibenate ShopHibimpl findById: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ShopHibimpl findById: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }
}
