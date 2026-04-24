package org.example.core.hibernate.documents.prices;

import jakarta.persistence.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.export.PriceHistoryByGoodAndShop;
import org.example.core.dto.export.ShopsCurrentPricesDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.exporting.ExportShopsCurrentPricesFilter;
import org.example.core.models.Price;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.JpaRoot;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PriceHibForExport extends HibernateAbstractDao<Price, Long, Logger> {
    private static final Logger logger = LogManager.getLogger(PriceHibForExport.class);

    protected PriceHibForExport() {
        super(Price.class);
    }

    @Transactional
    public List<ShopsCurrentPricesDto> getShopsCurrentPrices(ExportShopsCurrentPricesFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            StringBuilder sql = new StringBuilder();
            sql.append("""
                    SELECT g.id AS good_id, g.name AS good_name, p.price AS good_price, s.id AS shop_id 
                    """);
            buildSelect(filters, sql);

            sql.append(" FROM prices AS p INNER JOIN shops AS s ON p.shop_id = s.id INNER JOIN goods AS g ON p.good_id = g.id ");
            buildFrom(filters, sql);

            if (filters.getShopsIds() != null && !filters.getShopsIds().isEmpty()){
                sql.append("""
                         WHERE s.id IN (:shopsIds)
                        """);
            }
            var query = session.createNativeQuery(sql.toString(), Tuple.class);
            if (!filters.getShopsIds().isEmpty()){
                query.setParameterList("shopsIds", filters.getShopsIds());
            }

            List<Tuple> tuples = query.getResultList();
            return tuples.stream().map(t -> createDto(t, filters)).toList();


        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHibForExport getShopsCurrentPrices " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHibForExport getShopsCurrentPrices: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    private ShopsCurrentPricesDto createDto(
            Tuple t,
            ExportShopsCurrentPricesFilter filters
    ){
        ShopsCurrentPricesDto dto = new ShopsCurrentPricesDto();
        dto.setPrice(t.get("good_price", BigDecimal.class));
        dto.setGoodId(t.get("good_id", Long.class));
        dto.setGoodName(t.get("good_name", String.class));
        dto.setShopId(t.get("shop_id", Long.class));

        if (filters.isCategories()){
            dto.setCategoryId(t.get("category_id", Long.class));
            dto.setCategoryName(t.get("category_name", String.class));
            dto.setCategoryParentId(t.get("category_parent_id", Long.class));
        }

        if (filters.isShops()){
            dto.setShopName(t.get("shop_name", String.class));
            dto.setDistrictId(t.get("district_id", Long.class));
            dto.setDistrictName(t.get("district_name", String.class));
        }

        if (filters.isTags()){
            dto.setTags(t.get("tags", String.class));
        }

        return dto;
    }

    private void buildSelect(
            ExportShopsCurrentPricesFilter filters,
            StringBuilder builder
    ){
        if (filters.isShops()){
            builder.append("""
                     ,s.name AS shop_name, s.address AS shop_address,
                      d.id AS district_id , d.name  AS district_name
                    """);
        }

        if(filters.isCategories()){
            builder.append("""
                    ,c.id AS category_id , c.name AS category_name, c.parent_id AS category_parent_id 
                    """);
        }

        if (filters.isTags()){
            builder.append("""
                    , (SELECT STRING_AGG(t.name , ',')
                    FROM goods_tags gt JOIN tags t ON t.id = gt.tag_id WHERE gt.good_id = g.id) AS tags
                    """);
        }

    }

    private void buildFrom(
            ExportShopsCurrentPricesFilter filters,
            StringBuilder builder
    ){
        if (filters.isShops()){
            builder.append(" INNER JOIN districts AS d ON d.id = s.district_id ");
        }
        if(filters.isCategories()){
            builder.append(" INNER JOIN categories AS c ON c.id = g.category_id ");
        }
    }


    @Transactional
    public List<PriceHistoryByGoodAndShop> getPriceHistoryByGoodId(Long goodId, Long shopId){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<PriceHistoryByGoodAndShop> query = builder.createQuery(PriceHistoryByGoodAndShop.class);
            JpaRoot<Price> root = query.from(Price.class);

            List<JpaPredicate> predicates = new ArrayList<>();
            predicates.add(
                    builder.equal(root.get("good").get("id"), goodId)
            );
            predicates.add(
                    builder.equal(root.get("shop").get("id"), shopId)
            );

            query.select(builder.construct(PriceHistoryByGoodAndShop.class,
                    root.get("id"),
                    root.get("price"),
                    root.get("validTo"),
                    root.get("validFrom")
                    )).where(predicates.toArray(new JpaPredicate[0]));

            return session.createQuery(query).getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceHibForExport getPriceHistoryByGoodId " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceHibForExport getPriceHistoryByGoodId: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }


}
