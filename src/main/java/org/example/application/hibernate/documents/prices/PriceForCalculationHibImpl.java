package org.example.application.hibernate.documents.prices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.application.exceptions.CanNotMakeExecution;
import org.example.application.exceptions.NonHibernateException;
import org.example.application.hibernate.base_settings.HibernateAbstractDao;
import org.example.application.models.Good;
import org.example.application.models.Price;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PriceForCalculationHibImpl  extends HibernateAbstractDao<Price, Long, Logger> {
    private final static Logger logger = LogManager.getLogger(PriceForCalculationHibImpl.class);
    protected PriceForCalculationHibImpl() {
        super(Price.class);
    }

    @Transactional
    public Double recalculateForGood(Long goodId ){
        Session session = getSessionFactory().getCurrentSession();
        try{
            return session.createNativeQuery("""
        UPDATE goods SET rate = (
        SELECT COALESCE(AVG(CAST(r.rate AS double precision)), 0.0)
        FROM reviews AS  r
        WHERE r.good_id = goods.id AND  r.blocked = false)
        WHERE id = :goodId
        RETURNING rate
""", Double.class).setParameter("goodId", goodId).uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceAnalyseHibImpl recalculateForGood " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceAnalyseHibImpl recalculateForGood: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    //TODO false or NULL for blocked?
    public Map<Long, Good> recalculateForAllGoods(List<Long> goodIds ){
        Session session = getSessionFactory().getCurrentSession();
        try {
            List<Good> result = session.createNativeQuery("""
            
                            UPDATE goods
            SET rate = (
                SELECT COALESCE(AVG(CAST(r.rate AS double precision)), 0.0)
                FROM reviews r
                WHERE r.good_id = goods.id AND r.blocked = false
            )
            WHERE id IN (:goodIds)
            RETURNING *
            """, Good.class)
                    .setParameter("goodIds", goodIds).getResultList();
            return  result.stream()
                    .collect(Collectors.toMap(
                            Good::getId,
                            t -> t
                    ));
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в PriceAnalyseHibImpl recalculateForAllGoods " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception PriceAnalyseHibImpl recalculateForAllGoods: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }
}
