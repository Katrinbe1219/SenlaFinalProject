package org.example.core.hibernate.documents.subscriptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.subscriptions.AvailabilitySubFilter;
import org.example.core.models.AvailabilitySubscription;
import org.example.core.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AvailabilitySubHib extends HibernateAbstractDao<AvailabilitySubscription, Long, Logger> {
    private static final Logger logger = LogManager.getLogger(AvailabilitySubHib.class);
    protected AvailabilitySubHib() {
        super(AvailabilitySubscription.class);
    }

    @Transactional
    public List<AvailabilitySubscription> findAll(AvailabilitySubFilter filters){
        try{
              Session session = getSessionFactory().getCurrentSession();
              HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<AvailabilitySubscription> query = builder.createQuery(AvailabilitySubscription.class);
            JpaRoot<AvailabilitySubscription> root = query.from(AvailabilitySubscription.class);

            List<JpaPredicate> predicates = buildPredicates(filters,builder,root);
            JpaOrder order= buildOrder(filters,builder,root);

            query.select(root)
                    .where(predicates.toArray(new JpaPredicate[0]))
                    .orderBy(order);

            var squery = session.createQuery(query);
            if (filters.getPage() !=null && filters.getSize()!=0){
                squery
                        .setFirstResult(filters.getPage() * filters.getSize())
                        .setMaxResults(filters.getSize());

            }
            return squery.getResultList();

        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в AvailabilitySubHib findAll: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
            catch (Exception e){
            logger.error("NonHibernate Exception AvailabilitySubHib findAll: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    private JpaOrder buildOrder(
            AvailabilitySubFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<AvailabilitySubscription> root){

        return switch (filters.getSortType()){
            case ASC -> builder.asc(root.get("id"));
            case DESC -> builder.desc(root.get("id"));
            case CREATED_AT_ASC -> builder.asc(root.get("createdAt"));
            case CREATED_AT_DESC -> builder.desc(root.get("createdAt"));
            case USER_ID_ASC -> builder.asc(root.get("user").get("id"));
            case USER_ID_DESC -> builder.desc(root.get("user").get("id"));
            case GOOD_ID_ASC -> builder.asc(root.get("good").get("id"));
            case GOOD_ID_DESC -> builder.desc(root.get("good").get("id"));
            case SHOP_ID_ASC -> builder.asc(root.get("shop").get("id"));
            case SHOP_ID_DESC -> builder.desc(root.get("shop").get("id"));
        };
    }

    private List<JpaPredicate> buildPredicates(
            AvailabilitySubFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<AvailabilitySubscription> root
    ){
        List<JpaPredicate> predicates = new ArrayList<JpaPredicate>();

        if (filters.getShopIds() != null){
            predicates.add(
                    root.get("shop").get("id").in(filters.getShopIds())
            );

        }

        if (filters.getGoodIds() != null){
            predicates.add(
                    root.get("good").get("id").in(filters.getGoodIds())
            );
        }

        if (filters.getUserIds() != null){
            predicates.add(
                    root.get("user").get("id").in(filters.getUserIds())
            );
        }

        if (filters.getCurDate() != null){
            predicates.add(
                    builder.between(
                            root.get("createdAt"),
                            DateTimeUtils.toInstant(filters.getCurDate()),
                            DateTimeUtils.toInstantEndDay(filters.getCurDate())
                    )
            );
        }

        if (filters.getStartDate()!= null){
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.get("createdAt"),
                            DateTimeUtils.toInstant(filters.getStartDate())
                    )
            );
        }

        if (filters.getEndDate()!= null){
            predicates.add(
                    builder.lessThanOrEqualTo(
                            root.get("createdAt"),
                            DateTimeUtils.toInstantEndDay(filters.getEndDate())
                    )
            );
        }

        return predicates;
    };
}
