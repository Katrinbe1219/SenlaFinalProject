package org.example.core.hibernate.documents;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.RefreshTokenFilter;
import org.example.core.models.RefreshToken;
import org.example.core.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@DependsOn("liquibase")
public class RefreshTokenHibImpl extends HibernateAbstractDao<RefreshToken, Long, Logger> {
    private static final Logger logger = LogManager.getLogger(RefreshTokenHibImpl.class);

    protected RefreshTokenHibImpl() {
        super(RefreshToken.class);
    }

    @Transactional
    public List<RefreshToken> findAllFullVersion(){
        Session session = getSessionFactory().getCurrentSession();
        try{
            return session.createQuery("""
                SELECT DISTINCT rt FROM RefreshToken rt
                LEFT JOIN rt.user
            """, RefreshToken.class).getResultList();
        }
        catch (HibernateException e){
            logger.error("Проблема RefreshTokenHibImpl findAllFullVersion: " +  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RefreshTokenHibImpl findAllFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<RefreshToken> findAllByFilters(RefreshTokenFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<RefreshToken> query = builder.createQuery(RefreshToken.class);
            JpaRoot<RefreshToken> root = query.from(RefreshToken.class);

            List<JpaPredicate> predicates = buildPredicate(filters, builder, root);
            JpaOrder order = buildOrder(filters, builder, root);

            query.select(root)
                    .where(predicates.toArray(new JpaPredicate[predicates.size()]))
                    .orderBy(order);

            var squery = session.createQuery(query);
            if (filters.getSize() != null && filters.getPage()!= null){
                squery
                        .setMaxResults(filters.getSize())
                        .setFirstResult(filters.getSize()* filters.getPage());
            }

            return squery.getResultList();
        }
        catch (HibernateException e){
            logger.error("Проблема RefreshTokenHibImpl findAllByFilters: " +  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RefreshTokenHibImpl findAllByFilters: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public Optional<RefreshToken> findByUserAndDevice(Long userId, String deviceInfo) {
        Session session = getSessionFactory().getCurrentSession();
        try{
            return session.createQuery("""
            SELECT DISTINCT rt FROM RefreshToken rt
            LEFT JOIN rt.user
            WHERE rt.user.id = :id AND rt.deviceInfo =:device
            """, RefreshToken.class).setParameter("id", userId).setParameter("device", deviceInfo)
                    .uniqueResultOptional();
        }
        catch (HibernateException e){
            logger.error("Проблема RefreshTokenHibImpl findByUserAndDevice: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());

        }
        catch (Exception e){
            logger.error("NonHibernate Exception RefreshTokenHibImpl findByUserAndDevice: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        Session session = getSessionFactory().getCurrentSession();
        try{
            return session.createQuery("""
                SELECT rt FROM RefreshToken rt
               WHERE rt.tokenHash = :tokenHash
             """, RefreshToken.class).setParameter("tokenHash", tokenHash)
                    .uniqueResultOptional();
        }
        catch (HibernateException e){
            logger.error("Проблема RefreshTokenHibImpl findByTokenHash: " +  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RefreshTokenHibImpl findByTokenHash: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public Optional<RefreshToken> findFullByTokenHash(String tokenHash) {
        Session session = getSessionFactory().getCurrentSession();

        try{
            return session.createQuery("""
                SELECT DISTINCT rt FROM RefreshToken rt
               LEFT JOIN rt.user
              WHERE tokenHash = :tokenHash
             """, RefreshToken.class).setParameter("tokenHash", tokenHash)
                    .uniqueResultOptional();

        }
        catch (HibernateException e){
            logger.error("Проблема RefreshTokenHibImpl findFullByTokenHash: " +  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RefreshTokenHibImpl findFullByTokenHash: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public void deleteAllByUser(Long userId){
        Session session = getSessionFactory().getCurrentSession();

        try{
            int num = session.createMutationQuery("""

            DELETE FROM RefreshToken rt WHERE rt.user.id = :userId
""").setParameter("userId", userId).executeUpdate();
            if (num == 0){
                throw new DoesNoeExist("Nothing was deleted, check userId");
            }
        }
        catch(DoesNoeExist e){
            throw e;
        }
        catch (HibernateException e){
            logger.error("Проблема RefreshTokenHibImpl deleteAllByUser: " +  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RefreshTokenHibImpl deleteAllByUser: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<RefreshToken> getActiveSessionsByUser(Long userId){
        Session session = getSessionFactory().getCurrentSession();

        try{
            return session.createQuery("""
            SELECT DISTINCT rt FROM RefreshToken rt
            LEFT JOIN FETCH rt.user
            WHERE rt.user.id = :userId AND rt.expiresAt >= :time
""", RefreshToken.class).setParameter("userId", userId).setParameter("time", Instant.now())
                    .getResultList();
        }
        catch (HibernateException e){
            logger.error("Проблема RefreshTokenHibImpl getActiveSessionsByUser: " +  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception RefreshTokenHibImpl getActiveSessionsByUser: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    List<JpaPredicate> buildPredicate(
            RefreshTokenFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<RefreshToken> root
    ){
        List<JpaPredicate> predicates = new ArrayList<>();

        if (filters.getUserId()!=null){
            predicates.add(
                    builder.equal(root.get("user").get("id"), filters
                            .getUserId())
            );
        }

        if (filters.getUserIds() != null && !filters.getUserIds().isEmpty()){
            predicates.add(
                    root.get("id").in(filters.getUserIds())
            );
        }

        if (filters.getExpiresAt() != null){
            predicates.add(
                    builder.between(
                            root.get("expiresAt"),
                            DateTimeUtils.toInstant(filters.getExpiresAt()),
                            DateTimeUtils.toInstantEndDay(filters.getExpiresAt())
                    )
            );
        }

        if (filters.getStartExpiresAt() != null){
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.get("expiresAt"), DateTimeUtils.toInstant(filters.getStartExpiresAt())
                    )
            );
        }

        if (filters.getEndExpiresAt()!= null){
            predicates.add(
                    builder.lessThanOrEqualTo(
                            root.get("expiresAt"),
                            DateTimeUtils.toInstantEndDay(filters.getEndExpiresAt())
                    )
            );
        }


        if (filters.getCreatedAt() != null){
            predicates.add(
                    builder.between(
                            root.get("createdAt"),
                            DateTimeUtils.toInstant(filters.getCreatedAt()),
                            DateTimeUtils.toInstantEndDay(filters.getCreatedAt())
                    )
            );
        }

        if (filters.getStartCreatedAt() != null){
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.get("createdAt"), DateTimeUtils.toInstant(filters.getStartCreatedAt())
                    )
            );
        }

        if (filters.getEndCreatedAt()!= null){
            predicates.add(
                    builder.lessThanOrEqualTo(
                            root.get("createdAt"),
                            DateTimeUtils.toInstantEndDay(filters.getEndCreatedAt())
                    )
            );
        }


        if (filters.getLastUsedAt() != null){
            predicates.add(
                    builder.between(
                            root.get("lastUsedAt"),
                            DateTimeUtils.toInstant(filters.getLastUsedAt()),
                            DateTimeUtils.toInstantEndDay(filters.getLastUsedAt())
                    )
            );
        }

        if (filters.getStartLastUsedAt() != null){
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.get("lastUsedAt"), DateTimeUtils.toInstant(filters.getStartLastUsedAt())
                    )
            );
        }

        if (filters.getEndLastUsedAt()!= null){
            predicates.add(
                    builder.lessThanOrEqualTo(
                            root.get("lastUsedAt"),
                            DateTimeUtils.toInstantEndDay(filters.getEndLastUsedAt())
                    )
            );
        }

        return predicates;
    }

    private JpaOrder buildOrder(
            RefreshTokenFilter filters,
            HibernateCriteriaBuilder builder,
            JpaRoot<RefreshToken> root
    ){
        return switch (filters.getSortType()){
            case ASC -> builder.asc(root.get("id"));
            case DESC -> builder.desc(root.get("id"));
            case CREATED_AT_ASC -> builder.asc(root.get("createdAt"));
            case CREATED_AT_DESC -> builder.desc(root.get("createdAt"));
            case EXPIRES_AT_ASC -> builder.asc(root.get("expiresAt"));
            case EXPIRES_AT_DESC -> builder.desc(root.get("expiresAt"));
            case LAST_USED_AT_ASC -> builder.asc(root.get("lastUsedAt"));
            case LAST_USED_AT_DESC -> builder.desc(root.get("lastUsedAt"));
        };
    }

}
