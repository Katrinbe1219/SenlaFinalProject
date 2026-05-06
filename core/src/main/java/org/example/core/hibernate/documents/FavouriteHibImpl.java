package org.example.core.hibernate.documents;

import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.favourites.FavouriteCountByGoodDto;
import org.example.core.dto.getting.favourites.FavouriteGetForUserDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.FavouritesAnalystFilters;
import org.example.core.models.Favourite;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Repository
@DependsOn("liquibase")
public class FavouriteHibImpl extends HibernateAbstractDao<Favourite, Long, Logger> {
    private static final Logger logger = LogManager.getLogger(FavouriteHibImpl.class);

    protected FavouriteHibImpl() {
        super(Favourite.class);
    }

    @Transactional
    public void remove(Favourite fav){
        try{
            Session session = getSessionFactory().getCurrentSession();
            session.remove(fav);
            session.flush(); // принудительно
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в FavouriteHibImpl remove " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception FavouriteHibImpl remove: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<Favourite> findAllFullVersion(
             FavouritesAnalystFilters filters
    ){
        try{
            Session session = getSessionFactory().getCurrentSession();
            StringBuilder sql = new StringBuilder(
                    "SELECT DISTINCT f FROM Favourite f " +
                            "LEFT JOIN FETCH f.good g " +
                            "LEFT JOIN FETCH f.user u "
            );

            List<String> conditions = new ArrayList<>();

            if (filters.getTagIds() != null) {
                sql.append("JOIN g.tags t ");
                conditions.add("t.id IN (:tagIds)");
            }
            if (filters.getCategoryIds() != null) {
                conditions.add("g.category.id IN (:catIds)");
            }
            if (filters.getGoodIds() != null) {
                conditions.add("g.id IN (:goodIds)");
            }

            if (!conditions.isEmpty()) {
                sql.append("WHERE ").append(String.join(" AND ", conditions));
            }

            var query = session.createQuery(sql.toString(), Favourite.class);

            if (filters.getTagIds() != null)      query.setParameter("tagIds", filters.getTagIds());
            if (filters.getCategoryIds() != null) query.setParameter("catIds", filters.getCategoryIds());
            if (filters.getGoodIds() != null)     query.setParameter("goodIds", filters.getGoodIds());

            return query.getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в FavouriteHibImpl findAllFullVersion " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception FavouriteHibImpl findAllFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<FavouriteGetForUserDto> findAllByUser(Long userId){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT DISTINCT new org.example.core.dto.getting.favourites.FavouriteGetForUserDto(
            g.name, g.id )
            FROM Favourite f 
            JOIN f.good g 
            WHERE f.user.id = :userId
            """, FavouriteGetForUserDto.class).setParameter("userId", userId).getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в FavouriteHibImpl findAllByUser " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception FavouriteHibImpl findAllByUser: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public FavouriteGetForUserDto findByUserIdAndGoodId(Long userId, Long goodId){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT DISTINCT  new  org.example.core.dto.getting.favourites.FavouriteGetForUserDto(
            g.name, g.id )
            FROM Favourite  f 
            JOIN f.good g 
            WHERE f.user.id = :userId AND g.id = :goodId
            """, FavouriteGetForUserDto.class)
                    .setParameter("goodId", goodId).setParameter("userId", userId).uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в FavouriteHibImpl findByUserIdAndGoodId " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception FavouriteHibImpl findByUserIdAndGoodId: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public Favourite findByUserIdAndGoodIdPureVersion(Long userId, Long goodId){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT DISTINCT  f
            FROM Favourite  f 
            LEFT JOIN FETCH  f.good g 
            WHERE f.user.id = :userId AND g.id = :goodId
            """, Favourite.class)
                    .setParameter("goodId", goodId).setParameter("userId", userId).uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в FavouriteHibImpl findByUserIdAndGoodIdPureVersion " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception FavouriteHibImpl findByUserIdAndGoodIdPureVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<FavouriteCountByGoodDto> countAllByGoodId(
            FavouritesAnalystFilters filters
    ){
        try{
            Session session = getSessionFactory().getCurrentSession();
            StringBuilder sql = new StringBuilder("""
                    SELECT DISTINCT new  org.example.core.dto.getting.favourites.FavouriteCountByGoodDto(
            f.good.id, f.good.name, count(f.id))
            FROM Favourite f
                    """);

            List<String> conditions = new ArrayList<>();

            if (filters.getTagIds() != null) {
                sql.append(" JOIN f.good.tags t ");
                conditions.add("t.id IN (:tagIds)");
            }
            if (filters.getCategoryIds() != null) {
                conditions.add("f.good.category.id IN (:catIds)");
            }
            if (filters.getGoodIds() != null) {
                conditions.add("f.good.id IN (:goodIds)");
            }

            if (!conditions.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            sql.append(" GROUP BY f.good.id, f.good.name ");
            var query = session.createQuery(sql.toString(), FavouriteCountByGoodDto.class);

            if (filters.getTagIds() != null)      query.setParameter("tagIds", filters.getTagIds());
            if (filters.getCategoryIds() != null) query.setParameter("catIds", filters.getCategoryIds());
            if (filters.getGoodIds() != null)     query.setParameter("goodIds", filters.getGoodIds());

            return query.getResultList();
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в FavouriteHibImpl countByGoodId " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception FavouriteHibImpl countByGoodId: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }
    @Transactional
    //TODO for analyst
    public FavouriteCountByGoodDto countByGoodId(Long goodId){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT DISTINCT new  org.example.core.dto.getting.favourites.FavouriteCountByGoodDto(
            f.good.id, f.good.name, count(f.id))
            FROM Favourite f
            WHERE f.good.id = :goodId
            GROUP BY f.good.id, f.good.name
            """, FavouriteCountByGoodDto.class)
                    .setParameter("goodId", goodId)
                    .uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e) {
            logger.error("Hibernate Ошибка в FavouriteHibImpl countAllByGoodId " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception FavouriteHibImpl countAllByGoodId: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }


}
