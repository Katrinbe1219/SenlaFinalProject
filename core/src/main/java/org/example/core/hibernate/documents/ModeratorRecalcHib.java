package org.example.core.hibernate.documents;

import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.ManyModeratorLogCreateDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.ModeratorRecalcFilter;
import org.example.core.models.Good;
import org.example.core.models.ModeratorRatingCheck;
import org.example.core.models.User;
import org.example.core.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository

public class ModeratorRecalcHib extends HibernateAbstractDao<ModeratorRatingCheck, Long, Logger> {
    private static final Logger logger = LogManager.getLogger(ModeratorRecalcHib.class);
    protected ModeratorRecalcHib() {
        super(ModeratorRatingCheck.class);
    }

    @Value("${batchSize}")
    private Integer batchSize;

    @Transactional
    public void  createManyLogs(ManyModeratorLogCreateDto dto, User user){
        try{
            Session session = getSessionFactory().getCurrentSession();
            for (int i = 0; i< dto.getVerdicts().size(); i++){
                Good good = session.getReference(Good.class, dto.getVerdicts().get(i).getGoodId());

                ModeratorRatingCheck log  = new ModeratorRatingCheck();
                log.setCheckAt(Instant.now());
                log.setModerator(user);
                log.setGood(good);
                log.setComment(dto.getVerdicts().get(i).getComment());
                log.setVerdict(dto.getVerdicts().get(i).getVerdict());

                session.persist(log);
                if (i%batchSize ==0){
                    session.flush();
                    session.clear();
                }

            }
            session.flush();
            session.clear();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в ModeratorRecalcHib createManyLogs " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ModeratorRecalcHib createManyLogs: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<ModeratorRatingCheck> getModeratorRatingChecksByGoodIds(Set<Long> goodIds){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery(
                            "SELECT m FROM ModeratorRatingCheck m " +
                                    "WHERE m.good.id IN (:ids) " +
                                    "AND m.checkAt = (" +
                                    "SELECT MAX(m2.checkAt) FROM ModeratorRatingCheck m2 " +
                                    "WHERE m2.good.id = m.good.id" +
                                    ")",
                            ModeratorRatingCheck.class
                    )
                    .setParameter("ids", goodIds)
                    .getResultList();
        }catch(HibernateException e) {
            logger.error("Hibernate Ошибка в ModeratorRecalcHib getModeratorRatingChecksByGoodIds " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ModeratorRecalcHib getModeratorRatingChecksByGoodIds: "+e.getMessage());
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

            List<JpaPredicate> predicates = buildPredicates(filters, builder, root);
            JpaOrder order = buildOrder(filters, builder, root);
            root.fetch("good", JoinType.LEFT);
            root.fetch("moderator", JoinType.LEFT);

            query.select(root).where(predicates.toArray(new JpaPredicate[0])).orderBy(order);
            var squery = session.createQuery(query);
            if (filters.getPage() != null && filters.getCount() != null){
                squery.setFirstResult(filters.getPage()*filters.getCount())
                        .setMaxResults(filters.getCount());
            }

            return squery.getResultList() ;
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
