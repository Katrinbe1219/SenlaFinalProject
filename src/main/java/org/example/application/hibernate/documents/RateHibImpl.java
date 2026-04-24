package org.example.application.hibernate.documents;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.application.dto.getting.rates.RateFullDto;
import org.example.application.dto.getting.rates.RateInTimeDto;
import org.example.application.dto.getting.rates.RateValidGoodDto;
import org.example.application.exceptions.CanNotMakeExecution;
import org.example.application.exceptions.NonHibernateException;
import org.example.application.hibernate.base_settings.HibernateAbstractDao;
import org.example.application.hibernate.base_settings.filters.rates.RatesFilter;
import org.example.application.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.application.hibernate.base_settings.service_dto.RateExportDto;
import org.example.application.models.Good;
import org.example.application.models.RatingRecalculationLog;
import org.example.application.models.Review;
import org.example.application.models.types.RatingStatus;
import org.example.application.models.types.RatingTriggerType;
import org.example.application.utils.DateTimeUtils;
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
            for (Long goodsId: oldRates.keySet()) {
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
            predicates.add(builder.equal(root.get("ratingStatus"), RatingStatus.SUCCESS));

            query.select(builder.construct(
                    RateValidGoodDto.class,
                    root.get("id"),
                    root.get("recalculatedAt"),
                    root.get("newRate"),
                    root.get("triggeredBy")
                    )).where(builder.and(predicates.toArray(new JpaPredicate[0])))
                    .orderBy(builder.asc(root.get("newRate")));

            return session.createQuery(query)
                    .setMaxResults(count)
                    .list();
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
    public List<Long> getIdsByFilter(RatingRecalcFilter filters){
        Session session = getSessionFactory().getCurrentSession();
        try{
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Long> query = builder.createQuery(Long.class);
            JpaRoot<RatingRecalculationLog> root = query.from(RatingRecalculationLog.class);

            List<JpaPredicate> predicates = buildPredicates(builder, root, filters);
            query.select(root.get("id"))
                    .where(predicates.toArray(new JpaPredicate[0]));
            return session.createQuery(query).getResultList();
        }
        catch(HibernateException e){
            logger.error("Hibernate RateHinImpl getIdsByFilter " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RateHinImpl getIdsByFilter "+e.getMessage());
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

            List<Long> ids = getIdsByFilter(filters);
            query.select(builder.construct(
                    RateFullDto.class,
                    root.get("id"),
                    root.get("good").get("name"),
                    root.get("good").get("category").get("name"),
                    root.get("recalculatedAt"),
                    root.get("triggeredBy"),
                    root.get("ratingStatus"),
                    root.get("errorMessage"),
                    root.get("newRate")
            )).where(builder.and(root.get("id").in(ids)));
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

            List<Long> ids = getIdsByFilter(filters);
            query.select(builder.construct(
                    RateExportDto.class,
                    root.get("id"),
                    root.get("good").get("id"),
                    root.get("good").get("name"),
                    root.get("good").get("category").get("id"),
                    root.get("good").get("category").get("name"),
                    root.get("recalculatedAt"),
                    root.get("triggeredBy"),
                    root.get("ratingStatus"),
                    root.get("errorMessage"),
                    root.get("newRate")
            )).where(builder.and(root.get("id").in(ids)));
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

        if (filters.getMinDate() != null){
            start = DateTimeUtils.toInstant(filters.getMinDate());
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("recalculatedAt"), start)
            );
        }
        if (filters.getMaxDate() != null){
            end = DateTimeUtils.toInstantEndDay(filters.getMaxDate());
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

        if(filters.getGoodsIds() != null){
            predicates.add(
                    root.get("good").get("id").in(filters.getGoodsIds())
            );
        }

        if(filters.getCategoryIds() != null){
            predicates.add(
                    root.get("good").get("category").get("id").in(filters.getCategoryIds())
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
                            root.get("recalculatedAt"),DateTimeUtils.toInstantEndDay(filters.getLastDate())
                    ));
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.get("recalculatedAt"),DateTimeUtils.toInstant(filters.getFirstDate())
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
