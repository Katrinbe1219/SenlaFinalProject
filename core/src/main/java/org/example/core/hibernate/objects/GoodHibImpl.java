package org.example.core.hibernate.objects;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.rates.RateWithGoodNameDto;
import org.example.core.dto.getting.statistics.RecalculationForGoodDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.models.Category;
import org.example.core.models.Good;
import org.example.core.models.Tag;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.ModeratorVerdict;
import org.example.core.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@DependsOn("liquibase")
public class GoodHibImpl extends HibernateAbstractDao<Good, Long, Logger> {
    private static final Logger logger = LogManager.getLogger(GoodHibImpl.class);
    public GoodHibImpl() {
        super(Good.class);
    }

    @Transactional
    public Good getReferenceById(Long id) {
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.getReference(Good.class, id);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl getReferenceById " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl getReferenceById: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }


    @Transactional
    public List<RecalculationForGoodDto> getAllIdsForRecalculation(){

        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
                SELECT g.id AS goodId, g.rate AS rate FROM Good g WHERE g.moderatorStatus = :status
            """, RecalculationForGoodDto.class)
                    .setParameter("status", GoodStatusFromModerator.APPROVED)
                    .getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl getAllIdsForRecalculation " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl getAllIdsForRecalculation: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public Good findByIdFullVersion(Long id){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Good> query = builder.createQuery(Good.class);
            Root<Good> root = query.from(Good.class);
            root.fetch("unit", JoinType.LEFT);
            Fetch<Object, Object> catFetch =  root.fetch("category", JoinType.LEFT);
            catFetch.fetch("parent", JoinType.LEFT);
            root.fetch("tags", JoinType.LEFT);


            query.select(root).where(builder.equal(root.get("id"), id));

            return session.createQuery(query).uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl findByIdFullVersion " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl findByIdFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }


    }
    @Transactional
    public List<Long> findIdsByFilters(GoodFilter filters, boolean isUser){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Long> query = builder.createQuery(Long.class);
            JpaRoot<Good> root = query.from(Good.class);

            List<JpaPredicate> predicates = buildPredicates(filters, builder, root, isUser);
            JpaOrder order = buildOrder(filters, builder, root);
            query.select(root.get("id"))
                    .where(predicates.toArray(new JpaPredicate[0]))
                    .orderBy(order);
            var squery = session.createQuery(query);

            if (filters.getPage() != null && filters.getSize()!= null){
                squery.setFirstResult(filters.getPage()*filters.getSize()).setMaxResults(filters.getSize());
            }
            return squery.getResultList();


        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl findIdsByFilters " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl findIdsByFilters: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<Good> findAllByFilters(GoodFilter filters, boolean isUser){
        Session session = getSessionFactory().getCurrentSession();
        try{

            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Good> query = builder.createQuery(Good.class);
            JpaRoot<Good> root = query.from(Good.class);
            root.fetch("tags", JoinType.LEFT);
            root.fetch("category", JoinType.LEFT);

            root.fetch("unit", JoinType.LEFT);

            if (filters.getPage() != null && filters.getSize()!=null){
                List<Long> ids = findIdsByFilters(filters, isUser);
                if (ids.isEmpty()) return List.of();
                query.select(root).where(root.get("id").in(ids));

            }else{
                List<JpaPredicate> predicates = buildPredicates(filters, builder, root, isUser);
                JpaOrder order = buildOrder(filters, builder, root);
                query.select(root)
                        .where(predicates.toArray(new JpaPredicate[0]))
                        .orderBy(order);
            }


            return session.createQuery(query).getResultList();


        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl findAllForUserDto " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl findAllForUserDto: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    private GoodStatusFromModerator getStatus(ModeratorVerdict ver){
        return switch (ver){
            case SUSPICIOUS -> GoodStatusFromModerator.SUSPICIOUS;
            default -> GoodStatusFromModerator.APPROVED;
        };
    }
    @Transactional
    public void updateStatusForMany(Map<Long, ModeratorVerdict> dto){
        try{
            Session session = getSessionFactory().getCurrentSession();
            StringBuilder cases = new StringBuilder("CASE id ");
            dto.forEach((id, status) ->
                    cases.append(" WHEN ").append(id).append(" THEN '")
                            .append(getStatus(status).getValue()).append("' "));
            cases.append(" END ");
            String sql = "UPDATE goods SET moderator_status = " + cases +
                    " WHERE id IN (:ids)";
            session.createNativeQuery(sql)
                    .setParameter("ids", dto.keySet()).executeUpdate();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl updateStatusForMany " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl updateStatusForMany: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    // Rate-part finding
    @Transactional
    public List<RateWithGoodNameDto> findMaxRatesAmongAll(int count, boolean withSuspicious){
        Session session = getSessionFactory().getCurrentSession();
        try{
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<RateWithGoodNameDto> query = builder.createQuery(RateWithGoodNameDto.class);
            JpaRoot<Good> root = query.from(Good.class);
            query.select(builder.construct(RateWithGoodNameDto.class,
                    root.get("id"),
                    root.get("name"),
                    root.get("rate")
                            ))
            .orderBy(builder.desc(root.get("rate")));
            if (!withSuspicious){
                query.where(builder.equal(root.get("moderatorStatus"), GoodStatusFromModerator.APPROVED));
            }
            return session.createQuery(query).setMaxResults(count).getResultList();


        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl findMaxRatesAmongAll " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl findMaxRatesAmongAll: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    List<JpaPredicate> buildPredicates(
            GoodFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<Good> root,
            boolean isUser
    ){
        List<JpaPredicate> predicates = new ArrayList<JpaPredicate>();
        Instant timeConvert = null;
        Instant timeConvert1 = null;

        if (filters.getCategoryIds() != null){
            predicates.add(
                    builder.in(root.get("category").get("id"), filters.getCategoryIds())
            );
        }
        if (filters.getTagIds() != null){
            JpaJoin<Good, Tag> tagJoin = root.join("tags", JoinType.INNER);
            predicates.add(
                       tagJoin.get("id").in(filters.getTagIds())
                );
        }

        if (filters.getCurRating() != null){
            predicates.add(
                    builder.equal(root.get("rate"), filters.getCurRating())
            );
        }
        if (filters.getMaxRating() != null){
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("rate"), filters.getMaxRating())
            );
        }
        if (filters.getMinRating() != null){
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("rate"), filters.getMinRating())
            );
        }


        if (filters.getStartUpdatedAt()!= null){
            timeConvert = DateTimeUtils.toInstant(filters.getStartUpdatedAt());
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("updatedAt"), timeConvert)
            );
        }
        if (filters.getEndUpdatedAt()!= null){
            timeConvert = DateTimeUtils.toInstant(filters.getEndUpdatedAt().plusDays(1));
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("updatedAt"), timeConvert)
            );
        }
        if (filters.getCurUpdatedAt() != null){
            timeConvert = DateTimeUtils.toInstant(filters.getCurUpdatedAt());
            timeConvert1 = DateTimeUtils.toInstant(filters.getCurUpdatedAt().plusDays(1));

            predicates.add(
                    builder.between(root.get("updatedAt"), timeConvert, timeConvert1)
            );
        }

        if (filters.getEndCreatedAt() != null){
            timeConvert = DateTimeUtils.toInstant(filters.getEndCreatedAt().plusDays(1));
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("createdAt"), timeConvert)
            );
        }
        if (filters.getStartCreatedAt() != null){
            timeConvert = DateTimeUtils.toInstant(filters.getStartCreatedAt());
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("createdAt"), timeConvert)
            );
        }
        if (filters.getCurCreatedAt() != null){
            timeConvert = DateTimeUtils.toInstant(filters.getCurCreatedAt());
            timeConvert1 = DateTimeUtils.toInstant(filters.getCurCreatedAt().plusDays(1));
            predicates.add(
                    builder.between(root.get("createdAt"), timeConvert, timeConvert1)
            );
        }

        if (filters.getStatus() != null){
            predicates.add(
                    builder.equal(root.get("moderatorStatus"), filters.getStatus())
            );
        }

        if (isUser){
            predicates.add(
                    builder.equal(root.get("moderatorStatus"), GoodStatusFromModerator.APPROVED)
            );

        }
        return predicates;
    }

    private JpaOrder buildOrder(
            GoodFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<Good> root
    ){
        return switch (filters.getSortType()){
            case ASC -> builder.asc(root.get("id"));
            case DESC -> builder.desc(root.get("id"));
            case CREATED_AT_ASC -> builder.asc(root.get("createdAt"));
            case CREATED_AT_DESC -> builder.desc(root.get("createdAt"));
            case UPDATED_AT_ASC -> builder.asc(root.get("updatedAt"));
            case UPDATED_AT_DESC -> builder.desc(root.get("updatedAt"));
            case NAME_ASC -> builder.asc(root.get("name"));
            case NAME_DESC -> builder.desc(root.get("name"));
            case CAT_ASC -> {
                Join<Good, Category> catJoin = root.join("category", JoinType.LEFT);
                yield builder.asc(catJoin.get("id"));
            }
            case CAT_DESC -> {
                Join<Good, Category> catJoin = root.join("category", JoinType.LEFT);
                yield builder.desc(catJoin.get("id"));
            }
            case RATE_ASC -> builder.asc(root.get("rate"));
            case RATE_DESC -> builder.desc(root.get("rate"));
        };
    }




}
