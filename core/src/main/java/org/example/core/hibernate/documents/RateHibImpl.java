package org.example.core.hibernate.documents;


import jakarta.persistence.criteria.JoinType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.rates.RateFullDto;
import org.example.core.dto.getting.rates.RateInTimeDto;
import org.example.core.dto.getting.rates.RateValidGoodDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.rates.RatesFilter;
import org.example.core.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.core.hibernate.base_settings.service_dto.RateExportDto;
import org.example.core.models.*;
import org.example.core.models.types.RatingStatus;
import org.example.core.models.types.RatingTriggerType;
import org.example.core.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@DependsOn("liquibase")
@Repository
public class RateHibImpl extends HibernateAbstractDao<RatingRecalculationLog,Long, Logger> {
    private static final Logger logger = LogManager.getLogger(RateHibImpl.class);

    @Value("${batchSize}")
    private int batchSize;

    protected RateHibImpl() {
        super(RatingRecalculationLog.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // так как у нас ляжет транзакцию по обновлению рейтинга отдельного продукта
    // надо создавать отдельную транзакцию для логирования, чтобы она не легла вместе с той
    public void saveLog(Good good, String errorMessage, RatingStatus status,
                        RatingTriggerType type, Double oldRate, Double newRate ) {
        try{
            Session session = getSessionFactory().getCurrentSession();
            RatingRecalculationLog log = new RatingRecalculationLog();
            log.setRecalculatedAt(Instant.now());
            log.setGood(good);
            log.setErrorMessage(errorMessage);
            log.setRatingStatus(status);
            log.setTriggeredBy(type);
            log.setOldRate(oldRate);
            log.setNewRate(newRate);
            session.persist(log);
            session.flush();
        }
        catch(HibernateException e){
            logger.error("Hibernate RateHinImpl saveLog " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RateHinImpl saveLog "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // так как у нас ляжет транзакцию по обновлению рейтинга отдельного продукта
    // надо создавать отдельную транзакцию для логирования, чтобы она не легла вместе с той
    public void saveLogs(Map<Long, Good> newInfo, String errorMessage, RatingStatus status,
                         RatingTriggerType type, Map<Long, Double> oldRates ) {
        try{

            Session session = getSessionFactory().getCurrentSession();
            int count = 0;
            for (Long goodsId: newInfo.keySet()) {
                RatingRecalculationLog log = new RatingRecalculationLog();
                log.setRecalculatedAt(Instant.now());
                log.setGood(newInfo.get(goodsId));
                log.setErrorMessage(errorMessage);
                if (oldRates.get(goodsId) == null){
                    log.setRatingStatus(RatingStatus.FIRST_ADDED);
                }else{
                    log.setRatingStatus(status);
                }

                log.setTriggeredBy(type);
                log.setOldRate(oldRates.getOrDefault(goodsId,0.0));
                log.setNewRate(newInfo.get(goodsId).getRate());
                session.persist(log);
                count++;

                if (count%batchSize ==0){
                    session.flush();
                    session.clear();
                }
            }

            session.flush();
            session.clear();
        }
        catch(HibernateException e){
            logger.error("Hibernate RateHinImpl saveLogs " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RateHinImpl saveLogs "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // так как у нас ляжет транзакцию по обновлению рейтинга отдельного продукта
    // надо создавать отдельную транзакцию для логирования, чтобы она не легла вместе с той
    public void saveErrors(Map<Long, Good> info, String errorMessage, RatingStatus status,
                         RatingTriggerType type ) {
        try{
            Session session = getSessionFactory().getCurrentSession();
            int count = 0;
            for (Long goodsId: info.keySet()) {
                RatingRecalculationLog log = new RatingRecalculationLog();
                log.setRecalculatedAt(Instant.now());
                log.setGood(info.get(goodsId));
                log.setErrorMessage(errorMessage);
                log.setRatingStatus(status);
                log.setTriggeredBy(type);
                log.setOldRate(info.get(goodsId).getRate());
                log.setNewRate(null);
                session.persist(log);
                count++;

                if (count%batchSize ==0){
                    session.flush();
                    session.clear();
                }
            }

            session.flush();
            session.clear();
        }
        catch(HibernateException e){
            logger.error("Hibernate RateHinImpl saveErrors " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RateHinImpl saveErrors "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<RateValidGoodDto> findValidMaxRatesAmongProduct(int count, Long goodId){
        Session session = getSessionFactory().getCurrentSession();
        try{
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<RateValidGoodDto> query = builder.createQuery(RateValidGoodDto.class);
            JpaRoot<RatingRecalculationLog> root = query.from(RatingRecalculationLog.class);

            List<JpaPredicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("good").get("id"), goodId));
            predicates.add(
                    builder.or(root.get("ratingStatus").equalTo(RatingStatus.SUCCESS),
                            root.get("ratingStatus").equalTo(RatingStatus.FIRST_ADDED)));

            query.select(builder.construct(
                    RateValidGoodDto.class,
                    root.get("id"),
                    root.get("recalculatedAt"),
                    root.get("newRate"),
                    root.get("triggeredBy")
                    )).where(builder.and(predicates.toArray(new JpaPredicate[0])))
                    .orderBy(builder.desc(root.get("newRate")), builder.desc(root.get("recalculatedAt")));

            var squery = session.createQuery(query);
            if (count != 0){
                squery.setMaxResults(count);
            }

            return squery.list();
        }
        catch (HibernateException e) {
            logger.error("Hibernate RateHinImpl findMaxRatesAmongProduct " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RateHinImpl findMaxRatesAmongProduct "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }



    @Transactional
    public List<RateFullDto> getRatesByFilter(RatingRecalcFilter filters){
        Session session = getSessionFactory().getCurrentSession();
        try{
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<RateFullDto> query = builder.createQuery(RateFullDto.class);
            JpaRoot<RatingRecalculationLog> root = query.from(RatingRecalculationLog.class);

            JpaJoin<RatingRecalculationLog, Good> goodJoin = root.join("good", JoinType.LEFT);
            JpaJoin<Good, Category> categoryJoin = goodJoin.join("category", JoinType.LEFT);

            List<JpaPredicate> predicates = buildPredicates(builder, root, filters);

            query.select(builder.construct(
                    RateFullDto.class,
                    root.get("id"),
                    goodJoin.get("name"),
                    categoryJoin.get("name"),
                    root.get("recalculatedAt"),
                    root.get("triggeredBy"),
                    root.get("ratingStatus"),
                    builder.coalesce(root.get("errorMessage"), (String) null),
                    root.get("newRate")
            )).where(predicates.toArray(new JpaPredicate[0])).orderBy(builder.desc(root.get("recalculatedAt")));
            return session.createQuery(query).getResultList();
        }
        catch (HibernateException e){
            logger.error("RateHinImpl getGoodRatesByFilter " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RateHibImpl getGoodRatesByFilter "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<RateExportDto> getRatesForExport(RatingRecalcFilter filters){
        Session session = getSessionFactory().getCurrentSession();
        try{
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<RateExportDto> query = builder.createQuery(RateExportDto.class);
            JpaRoot<RatingRecalculationLog> root = query.from(RatingRecalculationLog.class);
            List<JpaPredicate> predicates = buildPredicates(builder, root, filters);

            JpaJoin<RatingRecalculationLog, Good> goodJoin = root.join("good", JoinType.LEFT);
            JpaJoin<Good, Category> categoryJoin = goodJoin.join("category", JoinType.LEFT);

            query.select(builder.construct(
                    RateExportDto.class,
                    root.get("id"),
                    goodJoin.get("id"),
                    goodJoin.get("name"),

                            categoryJoin.get("id"),
                            categoryJoin.get("name"),
                    root.get("recalculatedAt"),
                    root.get("triggeredBy"),
                    root.get("ratingStatus"),
                    builder.coalesce(root.get("errorMessage"), (String) null),
                    root.get("newRate")
            )).where(predicates.toArray(new JpaPredicate[0]));
            return session.createQuery(query).getResultList();
        }
        catch (HibernateException e){
            logger.error("RateHinImpl getRatesForExport " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RateHibImpl getRatesForExport "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }


    private List<JpaPredicate> buildPredicates(
            HibernateCriteriaBuilder builder,
            JpaRoot<RatingRecalculationLog> root,
            RatingRecalcFilter filters
    ){
        List<JpaPredicate> predicates = new ArrayList<>();
        Instant start;
        Instant end;
        if (filters.getCategoryId() != null){
            predicates.add(
                    builder.equal(root.get("good").get("category").get("id"),
                            filters.getCategoryId())
            );
        }

        if (filters.getGoodId() != null){
            predicates.add(
                    builder.equal(root.get("good").get("id"),
                            filters.getGoodId())
            );
        }

        if (filters.getCurDate() != null){
            start = DateTimeUtils.toInstant(filters.getCurDate());
            end = DateTimeUtils.toInstantEndDay(filters.getCurDate());
            predicates.add(
                    builder.between(
                            root.get("recalculatedAt"), start,end
                    )
            );
        }

        if (filters.getStartDate() != null){
            start = DateTimeUtils.toInstant(filters.getStartDate());
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("recalculatedAt"), start)
            );
        }
        if (filters.getEndDate() != null){
            end = DateTimeUtils.toInstantEndDay(filters.getEndDate());
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("recalculatedAt"), end)
            );
        }

        if (filters.getCurRate()!= null){
            predicates.add(
                    builder.equal(
                            root.get("newRate"), filters.getCurRate()
                    )
            );
        }
        if (filters.getMinRate()!= null){
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.get("newRate"), filters.getMinRate()
                    )
            );
        }
        if (filters.getMaxRate()!= null){
            predicates.add(
                    builder.lessThanOrEqualTo(
                            root.get("newRate"), filters.getMaxRate()
                    )
            );
        }

        if (filters.getStatus() != null){
            predicates.add(
                    builder.equal(root.get("ratingStatus"), filters.getStatus())
            );
        }
        if (filters.getTriggerType() != null){
            predicates.add(
                    builder.equal(root.get("triggeredBy"), filters.getTriggerType())
            );
        }

        if(filters.getGoodIds() != null){
            predicates.add(
                    root.get("good").get("id").in(filters.getGoodIds())
            );
        }

        if(filters.getCategoryIds() != null){
            predicates.add(
                    root.get("good").get("category").get("id").in(filters.getCategoryIds())
            );
        }

        if (filters.getTagId()!= null){
            JpaJoin< Good,Tag> tagJoin = root.join("good", JoinType.LEFT).join("tags", JoinType.LEFT);
            predicates.add(
                    tagJoin.get("id").equalTo(filters.getTagId())
            );
        }

        if (filters.getTagIds() != null){
            JpaJoin< Good,Tag> tagJoin = root.join("good", JoinType.LEFT).join("tags", JoinType.LEFT);
            predicates.add(
                    tagJoin.get("id").in(filters.getTagIds())
            );

        }
        return predicates;
    }

    @Transactional
    public List<RateInTimeDto> getGoodRateInTime(RatesFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<RateInTimeDto> query = builder.createQuery(RateInTimeDto.class);
            JpaRoot<RatingRecalculationLog> root = query.from(RatingRecalculationLog.class);

            List<JpaPredicate> predicates  = new ArrayList<>();
            predicates.add(builder.equal(root.get("good").get("id"), filters.getGoodId()));
            predicates.add(builder.
                    lessThanOrEqualTo(
                            root.get("recalculatedAt"),DateTimeUtils.toInstantEndDay(filters.getEndDate())
                    ));
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.get("recalculatedAt"),DateTimeUtils.toInstant(filters.getStartDate())
                    )
            );

            predicates.add(
                    builder.equal(
                            root.get("ratingStatus"), RatingStatus.SUCCESS
                    )
            );

            if (filters.getGoodStatus() != null){
                predicates.add(
                        builder.equal(
                                root.get("good").get("moderatorStatus"), filters.getGoodStatus()
                        )
                );
            }


            query.select(builder.construct(RateInTimeDto.class,
                    root.get("recalculatedAt"),
                    root.get("newRate"))).where(
                            predicates.toArray(new JpaPredicate[predicates.size()])
            ).orderBy(builder.asc(root.get("recalculatedAt")));

            return session.createQuery(query).getResultList();

        }
        catch (HibernateException e){
            logger.error("RateHinImpl getGoodRateInTime " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RateHibImpl getGoodRateInTime "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }




}
