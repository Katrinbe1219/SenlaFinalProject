package org.example.core.hibernate.objects;

import jakarta.persistence.criteria.JoinType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.models.Shop;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public  Shop getReferenceById(Long id){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.getReference(Shop.class, id);
        }
        catch(HibernateException e){
            logger.error("Hibenate ShopHibimpl getReferenceById: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ShopHibimpl getReferenceById: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<Shop> findAllFullVersion(Integer count, Integer page, BaseSortTypes filters,
                                         List<Long> ids, List<Long> districtIds) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Shop> query = builder.createQuery(Shop.class);
            JpaRoot<Shop> root = query.from(Shop.class);
            root.fetch("district", JoinType.LEFT);

            JpaOrder order = switch (filters){
                case ASC -> builder.asc(root.get("id"));
                case DESC -> builder.desc(root.get("id"));
                case NAME_ASC -> builder.asc(root.get("name"));
                case NAME_DESC -> builder.desc(root.get("name"));
            };

            List<JpaPredicate> predicates = new ArrayList<>();
            if (ids!=null && !ids.isEmpty()){
                predicates.add(root.get("id").in(ids));
            }

            if (districtIds!=null && !districtIds.isEmpty()){
                predicates.add(root.get("district").get("id").in(districtIds));
            }
            query.select(root)
                    .where(predicates.toArray(new JpaPredicate[0]))
                    .orderBy(order);
            // здесь пагинация работает нормально, так как связь manyToOne
            // - не подгружается куча всего из-за чего пагинация может сбиться
            var sQuery = session.createQuery(query);
            if (count != null && page != null){
                sQuery.setFirstResult(count*page)
                        .setMaxResults(count);
            }

            return sQuery.getResultList();
        }catch(HibernateException e){
            logger.error("Hibernate ShopHibImpl findAllFullVersion: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ShopHibImpl findAllFullVersion: "+e.getMessage());
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
            logger.error("Hibernate ShopHibImpl findById: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ShopHibImpl findById: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }
}
