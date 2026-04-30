package org.example.core.hibernate.objects;

import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.goods.GoodGetForUserDto;
import org.example.core.dto.getting.rates.RateWithGoodNameDto;
import org.example.core.dto.getting.statistics.RecalculationForGoodDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.models.Good;
import org.example.core.models.Tag;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@DependsOn("liquibase")
public class GoodHibImpl extends HibernateAbstractDao<Good, Long, Logger> {
    private static final Logger logger = LogManager.getLogger(GoodHibImpl.class);
    GoodHibImpl() {
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
    public List<RecalculationForGoodDto> getAllIds(){

        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
                SELECT g.id AS goodId, g.rate AS rate FROM Good g
            """, RecalculationForGoodDto.class).getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl getAllIds " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl getAllIds: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<RecalculationForGoodDto> getAllIdsForRecalculation(){

        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
                SELECT g.id AS goodId, g.rate AS rate FROM Good g WHERE g.moderatorStatus = :status
            """, RecalculationForGoodDto.class).setParameter("status", GoodStatusFromModerator.APPROVED).getResultList();
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
    public void updateRating(Long goodId, Double rating){
        try{
            Session session = getSessionFactory().getCurrentSession();
            session.createMutationQuery("""
UPDATE Good g SET g.rate = :rating WHERE g.id = :goodId
""").setParameter("rating", rating).setParameter("goodId", goodId).executeUpdate();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl updateRating " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl updateRating: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }



    @Transactional
    public GoodGetForUserDto getGoodForUserById(Long id) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try {
            // FETCH не нужен, STRING_ARG делает агрегацию
            return session.createQuery("""
                SELECT new org.example.core.dto.getting.goods.GoodGetForUserDto(
               g.id, g.name, c.name, u.fullName, g.rate, CAST (STRING_AGG(t.name, ',') as String), g.description
   ) FROM Good g
               LEFT JOIN   g.unit u 
              LEFT JOIN  g.category c 
             LEFT JOIN  g.tags t
          WHERE g.id = :id
          GROUP BY g.id, g.name, c.name, u.fullName, g.rate, g.description
       ORDER BY g.id
    
             
             """,
            GoodGetForUserDto.class).setParameter("id", id).uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl getGoodByIdFullVersion " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl getGoodByIdFullVersion: "+e.getMessage());
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

            List<JpaPredicate> predicates = buildPredicates(filters, builder, root, true);
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
    public List<GoodGetForUserDto> findAllForUserDto(GoodFilter filters){
        Session session = getSessionFactory().getCurrentSession();
        try{
            List<Long> ids = findIdsByFilters(filters, true);
            List<GoodGetForUserDto> res= session.createQuery("""
            SELECT new org.example.core.dto.getting.goods.GoodGetForUserDto(
            g.id, g.name,  c.name, u.fullName, g.rate, CAST(STRING_AGG(t.name, ',') as String), g.description
) FROM Good g
            LEFT JOIN g.category c
            LEFT JOIN g.unit u
            LEFT JOIN g.tags t
            WHERE g.id IN :ids
            GROUP BY g.id, g.name,  c.name, u.fullName, g.rate, g.description
             
            """, GoodGetForUserDto.class)
                    // array_position(ARRAY(SELECT unnest(:ids)), id) - POSTGRESQL спецификация, щлесь не подойдет
                    // отсортируем в  JAVA
                    .setParameter("ids", ids)

                    .getResultList();
            Map<Long, GoodGetForUserDto> maps = res.stream()
                    .collect(Collectors.toMap(
                            GoodGetForUserDto::getId, dto -> dto
                    ));

            return ids.stream().map(maps::get).filter(Objects::nonNull).toList();
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

    //TODO tags replace?
    @Transactional
    public List<Good> findAllForAnalyst(GoodFilter filters){
        Session session = getSessionFactory().getCurrentSession();
        try{
            List<Long> ids = findIdsByFilters(filters, false);
            if (ids.isEmpty()) return List.of();

            List<Good> res= session.createQuery("""
            SELECT DISTINCT  g FROM Good g
            LEFT JOIN FETCH g.category c
            LEFT JOIN FETCH g.unit u
            LEFT JOIN FETCH g.tags t
            WHERE g.id IN :ids
             
            """, Good.class)
                    // array_position(ARRAY(SELECT unnest(:ids)), id) - POSTGRESQL спецификация, щлесь не подойдет
                    // отсортируем в  JAVA
                    .setParameter("ids", ids)
                    .getResultList();
            Map<Long, Good> maps = res.stream()
                    .collect(Collectors.toMap(
                            Good::getId, dto -> dto
                    ));

            return ids.stream().map(maps::get).filter(Objects::nonNull).toList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в GoodHinImpl findAllForAnalyst " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception GoodHinImpl findAllForAnalyst: "+e.getMessage());
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
//            query.select(root)
            query.select(builder.construct(RateWithGoodNameDto.class,
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
            for(Long tagId: filters.getTagIds()){
                JpaJoin<Good, Tag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(
                       tagJoin.get("id").in(filters.getTagIds())
                );

            }

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

        if ((filters.getCurRating()!= null || filters.getMaxRating() != null || filters.getMinRating()!=null)
        && isUser){

            predicates.add(
                    builder.equal(root.get("moderatorStatus"), GoodStatusFromModerator.APPROVED)
            );
        }

        if (filters.getMinUpdatedAt()!= null){
            timeConvert = DateTimeUtils.toInstant(filters.getMinUpdatedAt());
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("updatedAt"), timeConvert)
            );
        }
        if (filters.getMaxUpdatedAt()!= null){
            timeConvert = DateTimeUtils.toInstant(filters.getMaxUpdatedAt().plusDays(1));
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

        if (filters.getMaxCreatedAt() != null){
            timeConvert = DateTimeUtils.toInstant(filters.getMaxCreatedAt().plusDays(1));
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("createdAt"), timeConvert)
            );
        }
        if (filters.getMinCreatedAt() != null){
            timeConvert = DateTimeUtils.toInstant(filters.getMinUpdatedAt());
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

        return predicates;
    }

    private JpaOrder buildOrder(
            GoodFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<Good> root
    ){
        return "asc".equalsIgnoreCase(filters.getSortType()) ?
                builder.asc(root.get("name"))
                : builder.desc(root.get("name"));
    }




}
