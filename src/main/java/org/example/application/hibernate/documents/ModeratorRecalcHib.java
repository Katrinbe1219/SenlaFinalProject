package org.example.application.hibernate.documents;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.application.exceptions.CanNotMakeExecution;
import org.example.application.exceptions.NonHibernateException;
import org.example.application.hibernate.base_settings.HibernateAbstractDao;
import org.example.application.hibernate.base_settings.filters.ModeratorRecalcFilter;
import org.example.application.models.ModeratorRatingCheck;
import org.example.application.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository

public class ModeratorRecalcHib extends HibernateAbstractDao<ModeratorRatingCheck, Long, Logger> {
    private static final Logger logger = LogManager.getLogger(ModeratorRecalcHib.class);
    protected ModeratorRecalcHib() {
        super(ModeratorRatingCheck.class);
    }


    @Transactional
    public  List<Long> getIdsByFilter(ModeratorRecalcFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Long> query = builder.createQuery(Long.class);
            JpaRoot<ModeratorRatingCheck> root = query.from(ModeratorRatingCheck.class);

            List<JpaPredicate> predicates = buildPredicates(filters, builder, root);
            JpaOrder order = buildOrder(filters, builder, root);

            query.select(root.get("id")).where(predicates.toArray(new JpaPredicate[0])).orderBy(order);
            var squery = session.createQuery(query);
            if (filters.getPage() != null && filters.getCount() != null){
                squery.setFirstResult(filters.getPage()*filters.getCount())
                        .setMaxResults(filters.getCount());
            }


            return squery.getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в ModeratorRecalcHib getIdsByFilter " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ModeratorRecalcHib getIdsByFilter: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<ModeratorRatingCheck> findAllFullVersion(ModeratorRecalcFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<ModeratorRatingCheck> query = builder.createQuery(ModeratorRatingCheck.class);
            JpaRoot<ModeratorRatingCheck> root = query.from(ModeratorRatingCheck.class);

            List<Long> ids = getIdsByFilter(filters);

            if (ids.isEmpty()){
                return List.of();
            }

            query.select(root).where(root.get("id").in(ids));
            List<ModeratorRatingCheck> list = session.createQuery(query).getResultList();
            Map<Long, ModeratorRatingCheck> maps = list.stream().collect(Collectors.toMap(
                    ModeratorRatingCheck::getId,
                    dto -> dto
            ));

            return ids.stream().map(maps::get).toList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в ModeratorRecalcHib findAllFullVersion " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ModeratorRecalcHib findAllFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    private List<JpaPredicate> buildPredicates(
            ModeratorRecalcFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<ModeratorRatingCheck> root
    ){
        List<JpaPredicate> predicates = new ArrayList<JpaPredicate>();

        if (filters.getModeratorId() != null){
            predicates.add(builder.equal(root.get("moderator").get("id"), filters.getModeratorId()));
        }

        if (filters.getModeratorIds() != null && !filters.getModeratorIds().isEmpty()){
            predicates.add(
                    root.get("moderator").get("id").in(filters.getModeratorIds()
                    )
            );
        }

        if (filters.getGoodId() != null){
            predicates.add(builder.equal(root.get("good").get("id"), filters.getGoodId()));
        }

        if (filters.getGoodIds() != null && !filters.getGoodIds().isEmpty()){
            predicates.add(
                    root.get("good").get("id").in(filters.getGoodIds())
            );
        }

        if (filters.getCurDate() != null){
            predicates.add(
                    builder.between(
                            root.get("checkAt"), DateTimeUtils.toInstant(filters.getCurDate()),
                            DateTimeUtils.toInstantEndDay(filters.getCurDate())
                    )
            );
        }

        if (filters.getStartDate() != null){
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.get("checkAt"), DateTimeUtils.toInstant(filters.getStartDate())
                    )
            );
        }

        if (filters.getEndDate() != null){
            predicates.add(
                    builder.lessThanOrEqualTo(
                            root.get("checkAt"), DateTimeUtils.toInstantEndDay(filters.getEndDate())
                    )
            );
        }

        if (filters.getVerdict() != null){
            predicates.add(
                    builder.equal(root.get("verdict"), filters.getVerdict())
            );
        }
        return predicates;

    }

    private JpaOrder buildOrder(
            ModeratorRecalcFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<ModeratorRatingCheck> root
    ){
        return switch (filters.getSortType()){
            case ASC -> builder.asc(root.get("id"));
            case DESC -> builder.desc(root.get("id"));
            case DATE_ASC -> builder.asc(root.get("checkAt"));
            case DATE_DESC -> builder.desc(root.get("checkAt"));
            case GOOD_ID_ASC -> builder.asc(root.get("good").get("id"));
            case GOOD_ID_DESC -> builder.desc(root.get("good").get("id"));
            case MODERATOR_ID_ASC -> builder.asc(root.get("moderator").get("id"));
            case MODERATOR_ID_DESC -> builder.desc(root.get("moderator").get("id"));

        };
    }
}
