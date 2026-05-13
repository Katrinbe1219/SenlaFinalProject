package org.example.core.hibernate.documents.prices;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.JoinType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.dto.getting.prices.PriceComparisonRequest;
import org.example.core.dto.getting.prices.PriceGetDtoForUser;
import org.example.core.dto.getting.prices.PriceGetResultForModerator;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.prices.PriceFilter;
import org.example.core.hibernate.base_settings.service_dto.CheckingPriceGoodShopExistence;
import org.example.core.models.Category;
import org.example.core.models.Good;
import org.example.core.models.Price;
import org.example.core.services.documents.prices.data.OptionForUpload;
import org.example.core.services.documents.prices.data.PriceCreateAllDto;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class PriceHibImpl extends HibernateAbstractDao<Price, Long, Logger> {
    private final static Logger logger = LogManager.getLogger(PriceHibImpl.class);
    protected PriceHibImpl() {
        super(Price.class);
    }

    @Value("${batchSize}")
    private int batchSize;


    @Transactional
    public List<Object[]> makeInvalidManyWithReturning(List<Long> goodIds, List<Long> shopIds){
        try{
            Session session = getSessionFactory().getCurrentSession();
           return  session.createNativeQuery("""
                UPDATE  prices 
                SET valid_to = :validTo
                WHERE (shop_id, good_id) IN 
                      (SELECT * FROM unnest(:shopIds, :goodIds))
                AND valid_to IS NULL
                RETURNING  good_id, shop_id, price;
                """)
                    .setParameter("goodIds", goodIds.toArray(Long[]::new))
                    .setParameter("shopIds", shopIds.toArray(Long[]::new))
                   .setParameter("validTo", Instant.now())
                    .getResultList();
        }
        catch(HibernateException e){
            logger.error("Hibernate PriceHibImpl makeInvalidManyWithReturning " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHibImpl makeInvalidManyWithReturning "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public void saveAll(List<PriceCreateDto> prices, OptionForUpload option, boolean isSkipped){
        try{
            Session session = getSessionFactory().getCurrentSession();

            String sql = switch (option){
                case SKIP -> "INSERT INTO prices (good_id, shop_id, price, valid_from) VALUES(?,?,?,?)" +
                        " ON CONFLICT (shop_id, good_id) WHERE valid_to IS NULL DO NOTHING";
                case STOP -> "INSERT INTO prices (good_id, shop_id, price, valid_from) VALUES(?,?,?,?)";
                default -> null;
            };


            if (sql != null){
                session.doWork(connection -> {
                    try (PreparedStatement ps = connection.prepareStatement(sql)){
                        int count = 0;

                        for (PriceCreateDto dto : prices){
                            ps.setObject(1, dto.getGoodId());
                            ps.setLong(2, dto.getShopId());
                            ps.setBigDecimal(3, dto.getPrice());
                            ps.setObject(4, LocalDateTime.now());
                            ps.addBatch();
                            count++;
                            if(count%batchSize ==0 ){
                                ps.executeBatch();
                            }

                        }

                        ps.executeBatch();

                    }
                    ;
                });
            }
            else{
                session.doWork(connection -> {
                    if (!isSkipped){
                        try (
                    PreparedStatement ps = connection.prepareStatement("""

                        UPDATE prices
                        SET valid_to = ?
                        WHERE shop_id= ? AND good_id = ? AND valid_to IS NULL
                """  )){
                            int count = 0;

                            for (PriceCreateDto dto : prices){
                                ps.setObject(1, LocalDateTime.now());
                                ps.setLong(2, dto.getShopId());
                                ps.setLong(3, dto.getGoodId());
                                ps.addBatch();
                                count++;
                                if(count%batchSize ==0 ){
                                    ps.executeBatch();
                                }

                            }


                            ps.executeBatch();
                        }
                    }


                    try(PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO prices (good_id, shop_id, price, valid_from) VALUES(?,?,?,?)
                    """)){

                        int count = 0;

                        for (PriceCreateDto dto : prices){
                            ps.setObject(1, dto.getGoodId());
                            ps.setLong(2, dto.getShopId());
                            ps.setBigDecimal(3, dto.getPrice());
                            ps.setObject(4, LocalDateTime.now());
                            ps.addBatch();
                            count++;
                            if(count%batchSize ==0 ){
                                ps.executeBatch();
                            }

                        }

                        ps.executeBatch();
                    }
                });
            }


        }
        catch(HibernateException e){
            if (e.getMessage().contains("violates foreign key")){
                throw new NotCorrectInput("Вы передали не существующие параметры")  ;
            }
            else if (e.getMessage().contains("violates unique constraint")){
                Pattern pattern = Pattern.compile(
                        "Key \\(shop_id, good_id\\)=\\((\\d+),\\s*(\\d+)\\)"
                );

                Matcher matcher = pattern.matcher(e.getMessage());
                if(matcher.find()){
                    throw new NotCorrectInput("Цена уже существует " + matcher.group(0));

                }else{
                    throw new NotCorrectInput("Цена уже существует " );

                }
            }
            logger.error("Hibernate PriceHibImpl saveAll " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHibImpl saveAll "+e.getMessage());
            if (option == OptionForUpload.STOP && e.getMessage().contains("duplicate key")) {
                throw new NotCorrectInput("Конфликт при импорте, операция остановлена");
            }
            throw new NonHibernateException(e.getMessage());
        }
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
    public List<PriceGetDtoForUser> getAllForUser(Long shopId, Long goodId) {
        Session session = getSessionFactory().getCurrentSession();
        try{
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
            JpaRoot<Price> root = query.from(Price.class);

            List<JpaPredicate> predicates = new ArrayList<>();
            if (shopId != null){
                predicates.add(builder.equal(root.get("shop").get("id"), shopId ));
            }
            if (goodId != null){
                predicates.add(builder.equal(root.get("good").get("id"), goodId));
            }
            predicates.add(builder.isNull(root.get("validTo")));


            query.multiselect(
                    root.get("shop").get("id").alias("shopId"),
                    root.get("shop").get("name").alias("shopName"),
                    root.get("shop").get("address").alias("address"),
                    root.get("good").get("name").alias("goodName"),
                    root.get("price").alias("price")
            ).where(builder.and(predicates.toArray(new JpaPredicate[0])));

            var squery =  session.createQuery(query);

            List<Tuple> tuples =squery.getResultList();

            return tuples.stream().map(tuple ->
                    new PriceGetDtoForUser(
                            tuple.get("shopId", Long.class),
                            tuple.get("shopName", String.class),
                            tuple.get("address", String.class),
                            tuple.get("goodName", String.class),
                            tuple.get("price", BigDecimal.class)
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
            if (request.getGoodId()!= null){
                predicates.add(builder.equal(root.get("good").get("id"), request.getGoodId()));
            }
            if (request.getShopIds() != null && !request.getShopIds().isEmpty()){
                predicates.add(root.get("shop").get("id").in(request.getShopIds()));
            }
            predicates.add(builder.isNull(root.get("validTo")));



            query.multiselect(
                    root.get("shop").get("id").alias("shopId"),
                    root.get("shop").get("name").alias("shopName"),
                    root.get("shop").get("address").alias("address"),
                    root.get("good").get("name").alias("goodName"),
                    root.get("price").alias("price")
            ).where(builder.and(predicates.toArray(new JpaPredicate[0])));

            List<Tuple> tuples = session.createQuery(query).getResultList();
            return tuples.stream().map(tuple ->
                    new PriceGetDtoForUser(
                            tuple.get("shopId", Long.class),
                            tuple.get("shopName", String.class),
                            tuple.get("address", String.class),
                            tuple.get("goodName", String.class),
                            tuple.get("price", BigDecimal.class)
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
    WITH shop_check AS (SELECT id FROM shops WHERE id = :shopId), 
         good_check AS (SELECT id FROM goods WHERE id = :goodId),
         price_check AS (SELECT id, price FROM prices WHERE good_id=:goodId AND shop_id=:shopId   AND valid_to IS NULL)
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
    public int makeInvalidPrice(Long goodId, Long shopId) {
        Session session = getSessionFactory().getCurrentSession();
        try{
            int num = session.createMutationQuery("""
              UPDATE Price p SET p.validTo = :newValidTo 
    WHERE p.good.id = :goodId AND p.shop.id = :shopId AND p.validTo is null 
    
          """).setParameter("goodId", goodId)
                    .setParameter("shopId", shopId)
                    .setParameter("newValidTo", Instant.now())
                    .executeUpdate();
            return num;
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHinImpl makeInvalidPrice(Long goodId, Long shopId) " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHinImpl makeInvalidPrice(Long goodId, Long shopId): "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public int makeInvalidPrice(Long priceId) {
        Session session = getSessionFactory().getCurrentSession();
        try{
            int num = session.createMutationQuery("""
              UPDATE Price p SET p.validTo = :newValidTo 
    WHERE p.id=:id AND p.validTo IS NULL
    
          """).setParameter("id", priceId)
                    .setParameter("newValidTo", Instant.now())
                    .executeUpdate();
            return num;
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHinImpl makeInvalidPrice(Long priceId " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHinImpl makeInvalidPrice(Long priceId: "+e.getMessage());
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

            List<JpaPredicate> predicates = buildPredicates(filters, minDate, maxDate, builder, root);
            JpaOrder order = buildOrder(builder, root, filters);
            JpaJoin<Good, Category> joinCat = root.join("good", JoinType.LEFT).join("category", JoinType.LEFT);
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
                    joinCat.get("name"),
                    joinCat.get("id")
                    )).where(predicates.toArray(new JpaPredicate[0])).orderBy(order);


            return session.createQuery(query).getResultList() ;
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

       return switch (filters.getSortType()) {
           case ASC -> builder.asc(root.get("id"));
           case DESC -> builder.desc(root.get("id"));
           case DATE_ASC -> builder.asc(root.get("validFrom"));
           case DATE_DESC -> builder.desc(root.get("validTo"));
           case PRICE_ASC -> builder.asc(root.get("price"));
           case PRICE_DESC -> builder.desc(root.get("price"));
       };
    }

}
