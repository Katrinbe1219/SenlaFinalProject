package org.example.core.hibernate.documents.prices;

import jakarta.persistence.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.prices.PriceComparisonRequest;
import org.example.core.dto.getting.prices.PriceGetDtoForUser;
import org.example.core.dto.getting.prices.PriceGetResultForModerator;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.prices.PriceFilter;
import org.example.core.hibernate.base_settings.service_dto.CheckingPriceGoodShopExistence;
import org.example.core.models.Price;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PriceHibImpl extends HibernateAbstractDao<Price, Long, Logger> {
    private final static Logger logger = LogManager.getLogger(PriceHibImpl.class);
    protected PriceHibImpl() {
        super(Price.class);
    }

    @Transactional
    public List<Price> findAllFullVersion(){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT p FROM Price p 
            LEFT JOIN p.good
            LEFT JOIN p.shop
            """, Price.class).getResultList();

        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHinImpl findAllFullVersion " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHinImpl findAllFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public PriceGetResultForModerator getByIdForModerator(Long id){
        Session session = getSessionFactory().getCurrentSession();
        try{
            return session.createQuery("""
              SELECT new org.example.core.dto.getting.prices.PriceGetResultForModerator(
                    p.id, p.price, p.validTo, p.validFrom, g.id , g.name,
                 s.id , s.name , s.address, c.name , c.id
    )
              FROM Price p 
              LEFT JOIN p.good g 
            LEFT JOIN p.good.category c 
          LEFT JOIN p.shop s
      WHERE p.id=:id
          """, PriceGetResultForModerator.class).setParameter("id", id).uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHinImpl getByIdForModerator " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHinImpl getByIdForModerator: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<PriceGetDtoForUser> getAllForUser(Long shopId, Long goodId, int count, int page) {
        Session session = getSessionFactory().getCurrentSession();
        try{
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
            JpaRoot<Price> root = query.from(Price.class);

            List<JpaPredicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("shop").get("id"), shopId ));
            predicates.add(builder.equal(root.get("good").get("id"), goodId));

            query.multiselect(
                    root.get("shop").get("name").alias("shopName"),
                    root.get("shop").get("address").alias("address"),
                    root.get("good").get("name").alias("goodName"),
                    root.get("price").alias("price")
            ).where(builder.and(predicates.toArray(new JpaPredicate[0])));
            List<Tuple> tuples = session.createQuery(query)
                    .setFirstResult(page*count)
                    .setMaxResults(count)
                    .getResultList();

            return tuples.stream().map(tuple ->
                    new PriceGetDtoForUser(
                            tuple.get("shopName", String.class),
                            tuple.get("address", String.class),
                            tuple.get("goodName", String.class),
                            tuple.get("price", Integer.class)
                    )).toList();

        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHinImpl getAllForUser " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHinImpl getAllForUser: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<PriceGetDtoForUser> compareByGoodAndShop(PriceComparisonRequest request){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
            JpaRoot<Price> root = query.from(Price.class);

            List<JpaPredicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("good").get("id"), request.getGoodId()));
            predicates.add(root.get("shop").get("id").in(request.getShopIds()));


            query.multiselect(
                    root.get("shop").get("name").alias("shopName"),
                    root.get("shop").get("address").alias("address"),
                    root.get("good").get("name").alias("goodName"),
                    root.get("price").alias("price")
            ).where(builder.and(predicates.toArray(new JpaPredicate[0])));

            List<Tuple> tuples = session.createQuery(query).getResultList();
            return tuples.stream().map(tuple ->
                    new PriceGetDtoForUser(
                            tuple.get("shopName", String.class),
                            tuple.get("address", String.class),
                            tuple.get("goodName", String.class),
                            tuple.get("price", Integer.class)
                    )).toList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHinImpl compareByGoodAndShop " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHinImpl compareByGoodAndShop: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    // for moderator--------------------------

    @Transactional
    public CheckingPriceGoodShopExistence checkBeforeAddPrice(Long shopId, Long goodId){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createNativeQuery("""
    WITH shop_check AS (SELECT id FROM shops WHERE id = :goodId), 
         good_check AS (SELECT id FROM goods WHERE id = :shopId),
         price_check AS (SELECT id, price FROM prices WHERE good_id=:goodId AND shop_id=:shopId)
     SELECT (SELECT id FROM shop_check) AS shopId,
              (SELECT id FROM good_check) AS goodId,
              (SELECT id FROM price_check) AS priceId,
              (SELECT price FROM price_check) AS price; 
""", CheckingPriceGoodShopExistence.class)
                    .setParameter("shopId", shopId)
                    .setParameter("goodId", goodId)
                    .uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHinImpl CheckingPriceGoodShopExistence " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHinImpl CheckingPriceGoodShopExistence: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }


    }
    @Transactional
    public Integer makeInvalidPrice(Long goodId, Long shopId) {
        Session session = getSessionFactory().getCurrentSession();
        try{
            Integer num = session.createMutationQuery("""
              UPDATE Price p SET p.validTo = :newValidTo 
    WHERE p.good.id = :goodId AND p.shop.id = :shopId AND p.validTo is null 
    
          """).setParameter("goodId", goodId)
                    .setParameter("shopId", shopId)
                    .setParameter("newValidTo", Instant.now())
                    .executeUpdate();
            return num;
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHinImpl makeInvalidPrice " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHinImpl makeInvalidPrice: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<Long> getIdsByFilter(PriceFilter filters, Instant minDate, Instant maxDate){
        Session session = getSessionFactory().getCurrentSession();
        try{
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Long> query = builder.createQuery(Long.class);
            JpaRoot<Price> root = query.from(Price.class);

            List<JpaPredicate> predicates = buildPredicates(filters, minDate, maxDate, builder, root);
            JpaOrder order = buildOrder(builder, root, filters);
            query.select(root.get("id")).where(predicates.toArray(new JpaPredicate[0])).orderBy(order);
            return session.createQuery(query)
                    .setFirstResult(filters.getPage()* filters.getSize())
                    .setMaxResults(filters.getSize())
                    .getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHinImpl getIdsByFilter " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHinImpl getIdsByFilter: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<PriceGetResultForModerator> getPricesByFilter(PriceFilter filters, Instant minDate, Instant maxDate){
        Session session = getSessionFactory().getCurrentSession();
        try{
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<PriceGetResultForModerator> query = builder.createQuery(PriceGetResultForModerator.class);
            JpaRoot<Price> root = query.from(Price.class);

            List<Long> ids = getIdsByFilter(filters, minDate, maxDate);
            query.select(builder.construct(PriceGetResultForModerator.class,
                    root.get("id"),
                    root.get("price"),
                    root.get("validFrom"),
                    root.get("validTo"),
                    root.get("good").get("id").alias("goodId"),
                    root.get("good").get("name").alias("goodName"),
                    root.get("shop").get("id").alias("shopId"),
                    root.get("shop").get("name").alias("shopName"),
                    root.get("shop").get("address"),
                    root.get("good").get("category").get("name"),
                    root.get("good").get("category").get("id")
                    )).where(root.get("id").in(ids));
            List<PriceGetResultForModerator > res = session.createQuery(query).getResultList();
            Map<Long, PriceGetResultForModerator> maps = res.stream().collect(Collectors.toMap(
                    PriceGetResultForModerator::getId, dto -> dto
            ));
            return ids.stream().map(maps::get).collect(Collectors.toList()) ;
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHinImpl getPricesByFilter " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHinImpl getPricesByFilter: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }
    private List<JpaPredicate> buildPredicates(
            PriceFilter filters,
            Instant minDate,
            Instant maxDate,
            HibernateCriteriaBuilder builder,
            JpaRoot<Price> root
    ){
        List<JpaPredicate> predicates = new ArrayList<>();
        if (filters.getCurrent() != null && filters.getCurrent()){
            predicates.add(
                    builder.isNull(root.get("validTo"))
            );
        }

        if (filters.getOld() != null && filters.getOld()){
            predicates.add(
                    builder.isNotNull(root.get("validTo"))
            );
        }

        if (filters.getCurPrice()!=null){
            predicates.add(
                    builder.between(root.get("price"), filters.getCurPrice(), filters.getCurPrice().add(new BigDecimal("0.99")))
            );
        }
        if (filters.getMaxPrice()!=null){
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("price"), filters.getMaxPrice())
            );
        }
        if (filters.getMinPrice()!=null){
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("price"), filters.getMinPrice())
            );
        }

        if (minDate != null){
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("validFrom"), minDate)
            );
        }
        if (maxDate != null){
            JpaPredicate first =  builder.lessThanOrEqualTo(root.get("validTo"), maxDate);
            JpaPredicate sec = builder.isNull(root.get("validTo"));
            predicates.add(
                    builder.or(first, sec)
            );
        }
        if (filters.getShopIds()!=null){
            predicates.add(
                    builder.in(root.get("shop").get("id"), filters.getShopIds())
            );
        }

        if (filters.getDistrictIds() != null){
            predicates.add(
                    builder.in(root.get("shop").get("district").get("id"), filters.getDistrictIds())
            );
        }

        if (filters.getGoodIds()!=null){
            predicates.add(
                    builder.in(root.get("good").get("id"), filters.getGoodIds())
            );
        }

        if (filters.getCategoryIds() != null){
            predicates.add(
                    builder.in(root.get("good").get("category").get("id"), filters.getCategoryIds())
            );
        }

        return predicates;
    }

    private JpaOrder buildOrder(
            HibernateCriteriaBuilder builder,
            JpaRoot<Price> root,
            PriceFilter filters
    ){

       return switch (filters.getSortDir()) {
           case ASC -> builder.asc(root.get("id"));
           case DESC -> builder.desc(root.get("id"));
           case DATE_ASC -> builder.asc(root.get("validFrom"));
           case DATE_DESC -> builder.desc(root.get("validTo"));
           case PRICE_ASC -> builder.asc(root.get("price"));
           case PRICE_DESC -> builder.desc(root.get("price"));
       };
    }

}
