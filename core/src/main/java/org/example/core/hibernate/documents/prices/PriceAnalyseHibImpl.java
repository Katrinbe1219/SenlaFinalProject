package org.example.core.hibernate.documents.prices;

import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.goods.GoodAnalyseForShopDto;
import org.example.core.dto.getting.goods.GoodPriceInShop;
import org.example.core.dto.getting.prices.PriceInTime;
import org.example.core.dto.getting.statistics.CartStatisticRequest;
import org.example.core.dto.getting.statistics.DistrictStatisticDto;
import org.example.core.dto.getting.statistics.categories.CategoryStatDto;
import org.example.core.dto.getting.statistics.shops.ShopCartDto;
import org.example.core.dto.getting.statistics.shops.ShopStatByCategoryDto;
import org.example.core.dto.getting.statistics.shops.ShopStatisticDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.filters.goods.GoodPriceInShopsFilter;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.prices.DistrictStatisticFilter;
import org.example.core.hibernate.base_settings.filters.prices.PriceInTimeFilter;
import org.example.core.hibernate.base_settings.filters.prices.ShopStatByCategoryFilter;
import org.example.core.models.Price;
import org.example.core.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.JpaRoot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class PriceAnalyseHibImpl extends HibernateAbstractDao<Price,Long, Logger> {
    private static  final Logger logger = LogManager.getLogger(PriceAnalyseHibImpl.class);
    protected PriceAnalyseHibImpl() {
        super(Price.class);
    }

    @Transactional
    public ShopStatisticDto getShopStatistic(Long shopId) {
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT  DISTINCT  new  org.example.core.dto.getting.statistics.shops.ShopStatisticDto (
            :shopId,
            MAX(p.price),
            MIN(p.price),
            CAST(AVG(p.price)  AS big_decimal)
            )
            FROM Price p
            WHERE p.shop.id = :shopId AND p.validTo IS NULL
            """, ShopStatisticDto.class)
                    .setParameter("shopId", shopId).uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceAnalyseHibImpl getShopStatistic " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceAnalyseHibImpl getShopStatistic: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<GoodAnalyseForShopDto> getExpensiveGoodsByShop(Long shopId, int count) {
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT DISTINCT  new org.example.core.dto.getting.goods.GoodAnalyseForShopDto (
                p.good.id,
                p.good.name,
                p.price,
                p.id
            ) FROM Price p
            WHERE p.shop.id = :shopId AND p.validTo IS NULL
            ORDER BY p.price DESC
            """, GoodAnalyseForShopDto.class)
                    .setMaxResults(count)
                    .setParameter("shopId", shopId).getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceAnalyseHibImpl getExpensiveGoodsByShop " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceAnalyseHibImpl getExpensiveGoodsByShop: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<GoodAnalyseForShopDto> getCheapestGoodsByShop(Long shopId, int count) {
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT DISTINCT  new org.example.core.dto.getting.goods.GoodAnalyseForShopDto (
                p.good.id,
                p.good.name,
                p.price,
                p.id
            ) FROM Price p
            WHERE p.shop.id = :shopId AND p.validTo IS NULL
            ORDER BY p.price ASC
            """, GoodAnalyseForShopDto.class)
                    .setMaxResults(count)
                    .setParameter("shopId", shopId).getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceAnalyseHibImpl getCheapestGoodsByShop " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceAnalyseHibImpl getCheapestGoodsByShop: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<ShopStatByCategoryDto> getShopsStatisticsBySubCategories(ShopStatByCategoryFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            StringBuilder sql = new StringBuilder();
            Map<String,Object> params = new LinkedHashMap<>();
            buildWithPartByFilters(sql, filters,params);

            Query query =  session.createNativeQuery(sql.toString(), Tuple.class);
            params.forEach(query::setParameter);

            List<Tuple> tuples = query.getResultList();

            Map<Long, ShopStatByCategoryDto> shopMap = new LinkedHashMap<>();
            for (Tuple tuple: tuples){
                Long shopId= tuple.get("shop_id", Long.class);
                String shopName = tuple.get("shop_name", String.class);
                ShopStatByCategoryDto shop = shopMap.computeIfAbsent(shopId,
                k -> new ShopStatByCategoryDto(shopId, shopName, new ArrayList<>())        );

                shop.getCategories().add(new CategoryStatDto(
                        tuple.get("category_id", Long.class),
                        tuple.get("category_name", String.class),
                        tuple.get("avg_price", BigDecimal.class),
                        tuple.get("min_price", BigDecimal.class),
                        tuple.get("max_price", BigDecimal.class),
                        tuple.get("product_count", Long.class)
                ));
            }

            return new ArrayList<>(shopMap.values());
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceAnalyseHibImpl getShopsStatisticsBySubCategories " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceAnalyseHibImpl getShopsStatisticsBySubCategories: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    private void buildWithPartByFilters(StringBuilder sql,
                                        ShopStatByCategoryFilter filters,
                                        Map<String, Object> params){

        if (filters.getCategoryIds() != null && !filters.getCategoryIds().isEmpty()){
            sql.append("WITH chosen_categories AS (SELECT * FROM categories AS c WHERE c.id IN (:categoryIds)  AND c.parent_id IS NOT NULL ),");
            params.put("categoryIds", filters.getCategoryIds());
        }else{
            sql.append("WITH chosen_categories AS (SELECT * FROM categories AS c WHERE c.parent_id IS NOT NULL ),");

        }
        if (filters.getShopIds() != null && !filters.getShopIds().isEmpty()){
            sql.append(" shops_chosen AS (SELECT s.id, s.name FROM shops AS s WHERE s.id IN (:shopIds) ) ");
            params.put("shopIds", filters.getShopIds());

        }else{
            sql.append(" shops_chosen AS (SELECT s.id, s.name FROM shops AS s ) ");

        }

        sql.append("""
                    
                        SELECT s.id AS shop_id,
                        s.name AS shop_name,
                         COALESCE(ROUND(AVG(p.price), 2), 0) AS avg_price,
                         COALESCE(ROUND(MAX(p.price), 2), 0) AS max_price,
                         COALESCE(ROUND(MIN(p.price), 2), 0) AS min_price,
                         COUNT(p.price) AS product_count,
                        ct.id AS category_id,
                        ct.name AS category_name
                        
                        FROM shops_chosen AS s 
                        CROSS JOIN chosen_categories AS ct
                        
                        LEFT JOIN goods AS g ON g.category_id = ct.id 
                        LEFT JOIN prices AS p ON p.good_id = g.id AND p.shop_id = s.id
                
                        
                        
                     
                    """);
        if (filters.getStartDate() != null && filters.getEndDate() != null){
            Instant start = DateTimeUtils.toInstant(filters.getStartDate());
            Instant end = DateTimeUtils.toInstantEndDay(filters.getEndDate());
            sql.append(
                    " WHERE p.valid_from <= :validTo" +
                            "   AND (p.valid_to IS NULL OR p.valid_to >= :validFrom)"
            );

            params.put("validFrom", start);
            params.put("validTo", end);
        }else{
            sql.append("""
                    WHERE p.valid_to IS NULL
                    """);
        }

        sql.append("GROUP BY s.id, s.name, ct.id, ct.name");
    }

    @Transactional
    public List<ShopStatByCategoryDto> getShopsStatisticsByMainCategories(ShopStatByCategoryFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            StringBuilder sql = new StringBuilder("""
                                WITH RECURSIVE category_tree AS (
            SELECT id, id as root_id, name as root_name
            FROM categories WHERE parent_id is NULL
                            
             UNION ALL
             
             SELECT c.id, ct.root_id, ct.root_name 
             FROM categories AS c 
             INNER JOIN category_tree AS ct ON ct.id = c.parent_id
            )
            
            
            SELECT s.id AS shop_id,
               s.name as shop_name,
               ct.root_id as root_id,
           ct.root_name as root_name,
COALESCE(ROUND(AVG(p.price), 2), 0) as avg_price,
COALESCE(max(p.price),0) as max_price,
COALESCE(min(p.price),0) as min_price,
CAST(COUNT(p.id) AS BIGINT) as product_count
            FROM shops AS s
            CROSS JOIN category_tree AS ct 
            LEFT JOIN goods AS g ON g.category_id = ct.id 
            LEFT JOIN prices AS p ON p.good_id = g.id AND p.shop_id = s.id 
""");
            Map<String,Object> params = new LinkedHashMap<>();
            setFiltersForShopStatByCategoryMainFilter(sql, filters,params);
             Query query =  session.createNativeQuery(sql.toString(), Tuple.class);
             params.forEach(query::setParameter);
            List<Tuple> tuples = query.getResultList();

            Map<Long, ShopStatByCategoryDto> shopMap = new LinkedHashMap<>();
            for (Tuple tuple: tuples){
                Long shopId= tuple.get("shop_id", Long.class);
                String shopName = tuple.get("shop_name", String.class);

                ShopStatByCategoryDto shop = shopMap.computeIfAbsent(shopId,
                k -> new ShopStatByCategoryDto(shopId, shopName, new ArrayList<>()));
                shop.getCategories().add(new CategoryStatDto(
                        tuple.get("root_id", Long.class),
                        tuple.get("root_name", String.class),
                        tuple.get("avg_price", BigDecimal.class),
                        tuple.get("min_price", BigDecimal.class),
                        tuple.get("max_price", BigDecimal.class),
                        tuple.get("product_count", Long.class)
                ));

            }

            return new ArrayList<>(shopMap.values());
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceAnalyseHibImpl getShopsStatisticsByMainCategories " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceAnalyseHibImpl getShopsStatisticsByMainCategories: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    private void setFiltersForShopStatByCategoryMainFilter(
            StringBuilder builder,
           ShopStatByCategoryFilter filters,
           Map<String,Object> params){

        if (filters.getStartDate() != null && filters.getEndDate() != null) {

            Instant start = DateTimeUtils.toInstant(filters.getStartDate());
            Instant end = DateTimeUtils.toInstantEndDay(filters.getEndDate());


            builder.append(
                    " AND  p.valid_from <= :validTo" +
                            "   AND (p.valid_to IS NULL OR p.valid_to >= :validFrom)"
            );
            params.put("validTo", end);
            params.put("validFrom", start);
        }else{
            builder.append(" AND p.valid_to IS NULL ");
        }

        builder.append(" WHERE ");
        boolean prev = false;

        if (filters.getShopIds()!= null && !filters.getShopIds().isEmpty()){
            builder.append("  p.shop_id IN (:shopIds) ");
            params.put("shopIds", filters.getShopIds());
            prev = true;
        }

        if (filters.getCategoryIds()!= null && !filters.getCategoryIds().isEmpty()){
            if (prev) builder.append(" AND  ");
            builder.append("  ct.root_id IN (:categoryIds) ");
            params.put("categoryIds", filters.getCategoryIds());
            prev = true;
        }

        if (!prev) builder.append(" 1=1 ");

        builder.append("""
                GROUP BY s.id, s.name, ct.root_id, ct.root_name
            ORDER BY s.id, ct.root_name""");


    }

    @Transactional
    public List<DistrictStatisticDto> getDistrictStatistic(DistrictStatisticFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            StringBuilder builder = new StringBuilder();

            if (filters.getCategoryIds() != null){
                builder.append("""
            WITH RECURSIVE cate AS (
            SELECT id, id as root_id, name as root_name, name , parent_id  FROM categories 
            WHERE id IN (:categoriesId) 
            
            UNION ALL SELECT c.id, ct.root_id,
         ct.root_name, c.name, c.parent_id  
         FROM categories AS c 
         INNER JOIN cate AS ct ON ct.id = c.parent_id ) 
                        
                        """);
            }else{
                builder.append("""
                        WITH RECURSIVE cate AS (
             SELECT id, id as root_id, name AS root_name
            FROM categories 
                            
             UNION ALL
             
             SELECT c.id, ct.root_id, ct.root_name
             FROM categories AS c 
             INNER JOIN cate AS ct ON ct.id = c.parent_id )
                        """);
            }
            //main
            builder.append("""
                    SELECT d.id,
                 d.name,
                 COALESCE(ROUND(MAX(p.price), 2), 0) as max_price,
            coalesce(ROUND(MIN(p.price), 2), 0) as min_price,
             COALESCE(ROUND(AVG(p.price), 2), 0) as avg_price,
             ct.root_name,
             ct.root_id
             
     FROM prices as p 
       JOIN shops as s ON s.id = p.shop_id
       JOIN districts as d ON d.id = s.district_id
        JOIN goods as g ON g.id = p.good_id 
        JOIN cate as ct ON ct.id = g.category_id 
        """);




        if (filters.getTagIds() != null && !filters.getTagIds().isEmpty()){
            builder.append(" JOIN goods_tags AS gt ON g.id = gt.good_id  JOIN tags as t ON t.id = gt.tag_id ");
        }

            Map<String, Object> params = new LinkedHashMap<>();
            setFilterForDistrictStatistic(filters, builder, params);

            Query query  = session.createNativeQuery(builder.toString(), DistrictStatisticDto.class);
            params.forEach(query::setParameter);

            return query.getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceAnalyseHibImpl getDistrictStatistic " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceAnalyseHibImpl getDistrictStatistic: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    private void setFilterForDistrictStatistic(
            DistrictStatisticFilter filters,
            StringBuilder builder,
            Map<String,Object> params
    ){
        if (filters.getStartDate() != null && filters.getEndDate() !=null){
            Instant start = DateTimeUtils.toInstant(filters.getStartDate());
            Instant end = DateTimeUtils.toInstantEndDay(filters.getEndDate());
            builder.append(
                    " WHERE p.valid_from <= :validTo" +
                            "   AND (p.valid_to IS NULL OR p.valid_to >= :validFrom)"
            );
            params.put("validFrom", start);
            params.put("validTo", end);
        }else{
            builder.append(" WHERE p.valid_to IS NULL ");
        }

        if (filters.getDistrictIds()!= null && !filters.getDistrictIds().isEmpty()){
            builder.append((" AND d.id IN (:districtsId) "));
            params.put("districtsId", filters.getDistrictIds());
        }

        if (filters.getGoodIds()!= null && !filters.getGoodIds().isEmpty()){
            builder.append((" AND g.id IN (:goodsId) "));
            params.put("goodsId", filters.getGoodIds());
        }

        if (filters.getTagIds()!= null && !filters.getTagIds().isEmpty()){
            builder.append((" AND t.id IN (:tagsIds) "));
            params.put("tagsIds", filters.getTagIds());
        }

        if (filters.getCategoryIds()!= null && !filters.getCategoryIds().isEmpty()){
            //builder.append((" AND c.id IN (:categoriesId) "));
            params.put("categoriesId", filters.getCategoryIds());
        }

        builder.append(" GROUP BY d.id, d.name,  ct.root_id, ct.root_name ORDER BY d.id");
    }

    @Transactional
    public List<ShopCartDto> compareCartInShops(CartStatisticRequest request){
        try{
            Session session = getSessionFactory().getCurrentSession();
            // cross join - декартово произведение
           return  session.createNativeQuery("""
                WITH cart_items AS(
        SELECT g.id, g.name , 1 as quantity FROM goods AS g WHERE g.id IN (:goodIds)
    ),
             store_prices AS (
            SELECT s.id AS shop_id, s.name AS shop_name, 
               p.price AS price, 
               ct.name AS good_name, 
           ct.quantity AS quantity,
               ct.id AS good_id,
           ct.quantity * COALESCE(p.price,0) AS good_price
                FROM shops AS s
                       CROSS JOIN cart_items AS ct 
                     LEFT JOIN prices AS p ON s.id = p.shop_id AND p.good_id = ct.id
                        AND p.valid_to IS NULL
            WHERE s.id IN (:shopIds)
        )
            
            SELECT shop_id AS shopId,
               shop_name AS shopName,
               SUM(coalesce(good_price,0)) AS totalPrice,
  
                STRING_AGG(
                    CASE WHEN price is NULL then good_name END, ','
                    ) AS outOfStockGoods 
         FROM store_prices
       GROUP BY shopId, shopName
            ORDER BY totalPrice DESC;
            
           
            """, ShopCartDto.class)
                    .setParameter("goodIds", request.getGoodIds())
                    .setParameter("shopIds", request.getShopIds())
                    .getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceAnalyseHibImpl compareCartInShops " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceAnalyseHibImpl compareCartInShops: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<PriceInTime> getGoodPriceInTime(PriceInTimeFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<PriceInTime> query = builder.createQuery(PriceInTime.class);
            JpaRoot<Price> root = query.from(Price.class);

            List<JpaPredicate> predicates = new ArrayList<>();
            predicates.add(
                    builder.equal(root.get("shop").get("id"), filters.getShopId())
            );
            predicates.add(
                    builder.equal(root.get("good").get("id"), filters.getGoodId())
            );

            predicates.add(
                    builder.lessThanOrEqualTo(
                            root.get("validFrom"),
                            DateTimeUtils.toInstantEndDay(filters.getEndDate())
                    )
            );

            JpaPredicate validToNull = builder.isNull(root.get("validTo"));
            JpaPredicate validToAfterStart = builder.greaterThanOrEqualTo(
                    root.get("validTo"),
                    DateTimeUtils.toInstant(filters.getStartDate())
            );
            predicates.add(builder.or(validToNull, validToAfterStart));
            predicates.add(
                    builder.or(validToNull,validToAfterStart)
            );


            query.select(builder.construct(PriceInTime.class,
                    root.get("price").as(Double.class),
                    root.get("validFrom"),
                    root.get("validTo")))
                    .where(predicates.toArray(new JpaPredicate[0])).orderBy(builder.asc(root.get("validFrom")));

            return session.createQuery(query).getResultList();

        }
         catch(HibernateException e) {
                logger.error("Hibernate Ошибка в PriceAnalyseHibImpl getGoodPriceInTime " + e.getMessage());
                throw new CanNotMakeExecution(e.getMessage());
            }
        catch (Exception e){
                logger.error("NonHibernate Exception PriceAnalyseHibImpl getGoodPriceInTime: "+e.getMessage());
                throw new NonHibernateException(e.getMessage());
            }
    }

    @Transactional
    public List<GoodPriceInShop> getGoodPricesInShops(GoodPriceInShopsFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<GoodPriceInShop> query = builder.createQuery(GoodPriceInShop.class);
            JpaRoot<Price> root = query.from(Price.class);

            List<JpaPredicate> predicates = new ArrayList<>();
            if (filters.getShopIds() != null && !filters.getShopIds().isEmpty()){
                predicates.add(
                        builder.in(root.get("shop").get("id"), filters.getShopIds())
                );
            }

            predicates.add(
                    builder.equal(root.get("good").get("id"), filters.getGoodId())
            );

            predicates.add(
                    builder.isNull(root.get("validTo"))
            );

            query.select(builder.construct(GoodPriceInShop.class,
                    root.get("shop").get("id"),
                    root.get("price").as(Double.class))).where(predicates.toArray(new JpaPredicate[0]));

            return session.createQuery(query).getResultList();


        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceAnalyseHibImpl getGoodPricesInShops " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceAnalyseHibImpl getGoodPricesInShops: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }




}
