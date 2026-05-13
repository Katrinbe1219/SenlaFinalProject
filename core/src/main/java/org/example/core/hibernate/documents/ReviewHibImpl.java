package org.example.core.hibernate.documents;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.ReviewCreateDto;
import org.example.core.dto.getting.reviews.ReviewDto;
import org.example.core.exceptions.*;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewForUserFilters;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewAdvancedFilters;
import org.example.core.models.Good;
import org.example.core.models.Review;
import org.example.core.models.User;
import org.example.core.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.criteria.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@DependsOn("liquibase")
@Repository
// blocked = True при блокировке
// сделать точный порядок при получении id И после 
public class ReviewHibImpl extends HibernateAbstractDao<Review, Long, Logger> {
    private static final Logger logger = LogManager.getLogger(ReviewHibImpl.class);

    protected ReviewHibImpl() {
        super(Review.class);
    }

    @Transactional
    public List<Review> findAllFullVersion() throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try {
            return session.createQuery(
                    """
                        SELECT DISTINCT r FROM Review r
                        LEFT JOIN FETCH r.good
                        LEFT JOIN FETCH r.user
                    """,
                    Review.class
            )
                    .getResultList();
        }
        catch(HibernateException e){
            logger.error("Проблема ReviewHIbImpl findAllFullVersion: "+  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ReviewHibImpl findAllFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public Review getByIdFullVersion(Long id) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try{
            return session.createQuery("""
                SELECT DISTINCT r FROM Review r
                LEFT JOIN FETCH r.good
                LEFT JOIN FETCH r.user
                WHERE r.id = :id
                """, Review.class).setParameter("id", id)
                    .uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e){
            logger.error("Проблема ReviewHIbImpl getByIdFullVersion: "+  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ReviewHibImpl getByIdFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<Review> getByUserSmallVersion(Long id, int page, int pageSize) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try {
            return session.createQuery("""
                SELECT DISTINCT r 
     FROM Review r WHERE r.user.id = :id
            """, Review.class)
                    .setParameter("id", id)
                    .setFirstResult(page*pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();
        }
        catch(HibernateException e){
            logger.error("Hibernate  ReviewHibImpl getByUserSmallVersion: "+  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ReviewHibImpl getByUserSmallVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public Review getByUserAndGood(Long userId, Long goodId) throws CanNotMakeExecution {
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT  r  FROM Review  r
            WHERE r.user.id = :id AND r.good.id=:goodId
            """, Review.class).setParameter("id", userId)
                    .setParameter("goodId", goodId)
                    .uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e){
            logger.error("Hibernate  ReviewHibImpl getByUserAndGood: "+  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ReviewHibImpl getByUserAndGood: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }


    @Transactional
    public boolean blockReview(Long id, User moderator){
        Session session = getSessionFactory().getCurrentSession();
        try {
            Review review = findById(id, logger);

            if (review == null) {
                throw new DoesNoeExist("Review does not exist with given credentials");
            }
            if (review.getBlocked()){
                throw new NotCorrectInput("This review is already blocked");
            }
            review.setBlocked(true);
            review.setBlockedAt(Instant.now());
            review.setBlockedBy(moderator);
            session.flush();
            return true;

         } catch (NotCorrectInput | DoesNoeExist e) {
            throw e;
        }
        catch (HibernateException e){
            logger.error("Hibernate ReviewHibImpl blockReview: "+  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ReviewHibImpl blockReview: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public void unblockReview(Long id, String login){
        Session session = getSessionFactory().getCurrentSession();
        try {
            Optional<Review> check = session.createQuery(
                    "SELECT  r FROM Review r " +
                            "LEFT JOIN FETCH r.user " +
                            " WHERE r.id=:id ")
                    .setParameter("id", id)
                    .uniqueResultOptional();

            if (check.isEmpty()) {
                throw new DoesNoeExist("Review does not exist with given credentials");
            }

            Review review=  check.get();
            if (!review.getBlocked()){
                throw new NotCorrectInput("This review is not blocked");
            }
            if (review.getBlockedBy() == null){
                logger.error("ReviewHibImpl unblock good is blocked but blockedBy is null ");
                throw new NonHibernateException("ReviewHibImpl unblock good is blocked but blockedBy is null ");
            }

            if ( !review.getBlockedBy().getLogin().equals(login)){
                throw new PermissionDenied("This review was blocked not by you");
            }
            review.setBlocked(false);
            review.setBlockedBy(null);
            review.setBlockedAt(null);
            session.flush();


        }
        catch(DoesNoeExist | PermissionDenied  | NotCorrectInput e){
            throw e;
        }
        catch (HibernateException e){
            logger.error("Hibarenate ReviewHibImpl unblockReview: "+  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ReviewHibImpl unblockReview: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<Review> getFullByFilters(ReviewAdvancedFilters filters) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try{

            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Review> query = builder.createQuery(Review.class);
            JpaRoot<Review> root = query.from(Review.class);

            root.fetch("user", JoinType.LEFT);
            root.fetch("good", JoinType.LEFT);
            root.fetch("blockedBy", JoinType.LEFT);

            List<JpaPredicate> predicates = buildPredicates(builder, root, filters);
            query.select(root)
                    .where(builder.and(predicates.toArray(new Predicate[0]))) // java потом сама поставит размер
                    .orderBy(buildOrder(builder, root, filters));
            var squery = session.createQuery(query);
            if (filters.getPage() != null && filters.getSize() != null){
                squery.setFirstResult(filters.getPage()*filters.getSize())
                        .setMaxResults(filters.getSize());
            }
            return squery.getResultList();

        }
        catch (HibernateException e){
            logger.error("Hibernate ReviewHibImpl getFullByFilters: "+  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ReviewHibImpl getFullByFilters: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }



    @Transactional
    public List<Review> getMinByFilters(ReviewForUserFilters filters) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try{

            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Review> query = builder.createQuery(Review.class);
            JpaRoot<Review> root = query.from(Review.class);

            List<JpaPredicate> predicates = buildPredicates(builder, root, filters);
            predicates.add(builder.equal(root.get("blocked"), false));

            query.select(root)
                    .where(builder.and(predicates.toArray(new JpaPredicate[0])))
                    .orderBy(builderOrder(builder, root, filters));

            var squery = session.createQuery(query);
            if (filters.getPage()!= null && filters.getSize() != null){
                squery.setFirstResult(filters.getPage()*filters.getSize())
                        .setMaxResults(filters.getSize());
            }
            return squery.getResultList();

        }
        catch(HibernateException e){
            logger.error("Hibernate ReviewHibImpl getMinByFilters: "+  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ReviewHibImpl getMinByFilters: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }



    @Transactional
    public Review createReview(ReviewCreateDto dto, Good good, User user){
        Session session = getSessionFactory().getCurrentSession();
        try{
            Review review = new Review();
            review.setBlocked(false);
            review.setGood(good);
            review.setRate(dto.getRate());
            review.setReview(dto.getReview());
            review.setCreatedAt(Instant.now());
            review.setUser(user);
            session.persist(review);
            return review;
        }
        catch(ConstraintViolationException e){
            throw new NotCorrectInput("Your review on current good already exist");
        }
        catch(HibernateException e){
            logger.error("Hibernate ReviewHibImpl createReview: "+  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ReviewHibImpl createReview: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public void deleteReview(Long goodId, Long userId){
        Session session = getSessionFactory().getCurrentSession();
        try{
            Optional<Review> review = session.createQuery("""
            SELECT r FROM Review  r
            WHERE r.good.id = :goodId AND r.user.id = :userId
""", Review.class).setParameter("goodId", goodId).setParameter("userId", userId).uniqueResultOptional();
            if (review.isEmpty()) throw new NotCorrectInput("Не был найден отзыв");
            session.remove(review.get());
        }
        catch (HibernateException e){
            logger.error("Hibernate ReviewHibImpl deleteReview: "+  e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (NotCorrectInput e){

            throw new DoesNoeExist("Review was not found with given credentials" );
        }
        catch (Exception e){
            logger.error("NonHibernate Exception ReviewHibImpl deleteReview: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    private List<JpaPredicate> buildPredicates(
            HibernateCriteriaBuilder builder,
            JpaRoot<Review> root,
            ReviewForUserFilters filters
    ){
        List<JpaPredicate> predicates = new ArrayList<>();
        Instant firstDate;
        Instant endDate;

        if(filters.getGoodId() != null){
            predicates.add(
                    builder.equal(root.get("good").get("id"), filters.getGoodId())
            );
        }

        if (filters.getRate() != null){
            predicates.add(
                    builder.equal(root.get("rate"), filters.getRate())
            );
        }

        if (filters.getFirstDate() != null){
            firstDate = DateTimeUtils.toInstant(filters.getFirstDate());
            predicates.add(
                    builder.greaterThan(root.get("createdAt"), firstDate)
            );
        }

        if (filters.getLastDate() != null){
            endDate = DateTimeUtils.toInstantEndDay(filters.getLastDate());
            predicates.add(
                    builder.lessThan(root.get("createdAt"), endDate)
            );
        }

        if (filters.getReviewDate() != null){
            firstDate = DateTimeUtils.toInstant(filters.getReviewDate());
            endDate = DateTimeUtils.toInstantEndDay(filters.getReviewDate());
            predicates.add(
                    builder.between(root.get("createdAt"), firstDate, endDate)
            );
        }
        return predicates;
    }

    private JpaOrder builderOrder(
            HibernateCriteriaBuilder builder,
            JpaRoot<Review> root,
            ReviewForUserFilters filters
    ){
        return "asc".equalsIgnoreCase(filters.getSortType())
                ? builder.asc(root.get("id"))
                : builder.desc(root.get("id"));
    }



    private List<JpaPredicate> buildPredicates(
            HibernateCriteriaBuilder builder,
            JpaRoot<Review> root,
            ReviewAdvancedFilters filters)
    {
        List<JpaPredicate> predicates = new ArrayList<>();
        Instant firstDate;
        Instant endDate;

        if (filters.getBlocked() != null) {
            predicates.add(
                    builder.equal(root.get("blocked"), filters.getBlocked())
            );
        }

        if (filters.getBlockedBy() != null) {
            predicates.add(
                    builder.equal(root.get("blockedBy"), filters.getBlockedBy())
            );
        }

        if (filters.getBlockedAt() != null) {
            firstDate = DateTimeUtils.toInstant(filters.getBlockedAt());
            endDate = DateTimeUtils.toInstantEndDay(filters.getBlockedAt());
            predicates.add(
                    builder.between(root.get("blockedAt"), firstDate, endDate)
            );
        }

        if (filters.getRate() != null) {
            predicates.add(
                    builder.equal(root.get("rate"), filters.getRate())
            );
        }

        if (filters.getGoodId() != null) {
            predicates.add(
                    builder.equal(root.get("good").get("id"), filters.getGoodId())
            );
        }

        if (filters.getUserId() != null){
            predicates.add(
                    builder.equal(root.get("user").get("id"), filters.getUserId())
            );
        }

        if (filters.getStartDate() != null){
            firstDate = DateTimeUtils.toInstant(filters.getStartDate());
            predicates.add(
                    builder.greaterThan(
                            root.get("createdAt"),firstDate
                    )
            );
        }

        if (filters.getEndDate() != null){
            endDate = DateTimeUtils.toInstantEndDay(filters.getEndDate());
            predicates.add(
                    builder.lessThanOrEqualTo(
                            root.get("createdAt"), endDate
                    )
            );
        }

        if (filters.getCreatedAt() != null){
            firstDate = DateTimeUtils.toInstant(filters.getCreatedAt());
            endDate = DateTimeUtils.toInstantEndDay(filters.getCreatedAt());
            predicates.add(
                    builder.between(root.get("createdAt"), firstDate, endDate)
            );
        }

        return predicates;
    }

    private JpaOrder buildOrder(
            HibernateCriteriaBuilder builder,
            JpaRoot<Review> root,
            ReviewAdvancedFilters filters
    ){

        return switch (filters.getSortType()){
            case ASC -> builder.asc(root.get("id"));
            case DESC -> builder.desc(root.get("id"));
            case GOOD_ASC -> builder.asc(root.get("good").get("id"));
            case GOOD_DESC -> builder.desc(root.get("good").get("id"));
            case USER_ID_ASC -> builder.asc(root.get("user").get("id"));
            case USER_ID_DESC -> builder.desc(root.get("user").get("id"));
            case RATE_ASC -> builder.asc(root.get("rate"));
            case RATE_DESC -> builder.desc(root.get("rate"));
            case CREATED_AT_ASC -> builder.asc(root.get("createdAt"));
            case CREATED_AT_DESC -> builder.desc(root.get("createdAt"));
        };
    }
}
