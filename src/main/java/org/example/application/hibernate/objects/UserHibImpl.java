package org.example.application.hibernate.objects;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.application.dto.patching.UpdateUserPasswordDto;
import org.example.application.dto.export.ModeratorDto;
import org.example.application.exceptions.CanNotMakeExecution;
import org.example.application.exceptions.NonHibernateException;
import org.example.application.hibernate.base_settings.HibernateAbstractDao;
import org.example.application.hibernate.base_settings.filters.UserForModeratorFilter;
import org.example.application.models.Role;
import org.example.application.models.User;
import org.example.application.models.types.RoleTypes;
import org.example.application.utils.DateTimeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@DependsOn("liquibase")
public class UserHibImpl extends HibernateAbstractDao<User, Long, Logger> {
    private static final Logger logger = LogManager.getLogger(UserHibImpl.class);
    UserHibImpl() {
        super(User.class);
    }


    @Transactional
    public Role getDefaultRole(){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("SELECT r FROm Role  r WHERE r.name = :role", Role.class)
                    .setParameter("role", "MIN_USER").uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl getDefaultRole: " + e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getDefaultRole: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public User getUserByUsernameFullVersion(String username) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<User> query = builder.createQuery(User.class);
            JpaRoot<User> root = query.from(User.class);
            query.where(builder.equal(root.get("username"), username));
//            Hibernate.initialize(root.get("role")); // - Не работает, тут просто путь, другое дело если бы было user.getRole
            root.fetch("role", JoinType.LEFT);

            Optional<User> result = session.createQuery(query).uniqueResultOptional();
            return result.orElse(null);

        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl getUserByUsernameFullVersion: " + e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getUserByUsernameFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<User> findByUsernameOrLoginOrEmail(String login, String username, String email){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
                SELECT u FROM User u
               WHERE u.username = :username OR u.login = :login OR u.email = :email
             """, User.class).setParameter("login", login)
                    .setParameter("username", username).setParameter("email", email).getResultList();
        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl findByUsernameOrLoginOrEmail: " + e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl findByUsernameOrLoginOrEmail: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }


    @Transactional
    public User getUserByLoginFullVersion(String login) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<User> query = builder.createQuery(User.class);
            JpaRoot<User> root = query.from(User.class);
            query.where(builder.equal(root.get("login"), login));
            root.fetch("role", JoinType.LEFT);

            Optional<User> result = session.createQuery(query).uniqueResultOptional();
            if (result.isPresent()){
                return result.get();
            }
            throw new UsernameNotFoundException("User with login " + login + " not found");

        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl getUserByLoginFullVersion: " + e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getUserByLoginFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }


    @Transactional
    public User getByUsernameSmallVersion(String username) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try{
            return session.createQuery("""
                SELECT u FROM User u WHERE u.username = :username
""", User.class).setParameter("username", username).uniqueResultOptional().orElse(null);
        }
        catch (HibernateException e){
            logger.error("Hibernate UserHibImpl getByUsernameSmallVersion: " + e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getByUsernameSmallVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public User getByLoginSmallVersion(String login) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try{
            return session.createQuery("""
            SELECT u FROM User u WHERE u.login = :login
            """, User.class).setParameter("login", login).uniqueResultOptional().orElse(null);
        }
        catch (HibernateException e){
            logger.error("Hibernate UserHibImpl getByLoginSmallVersion: " + e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getByLoginSmallVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public User getByEmailSmallVersion(String email) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try{
            return session.createQuery("""
            SELECT u FROM User u WHERE u.email = :email
            """, User.class).setParameter("email", email).uniqueResultOptional().orElse(null);
        }
        catch (HibernateException e){
            logger.error("Hibernate UserHibImpl getByEmailSmallVersion: " + e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getByEmailSmallVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }


    @Transactional
    public Long getUserIdByUsername(String username) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try{
            return session.createQuery("""
                SELECT u.id FROM User u WHERE u.username = :username
             """, Long.class).setParameter("username", username).uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl getUserIdByUsername: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getUserIdByUsername: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public Long getUserIdByLogin(String login) throws CanNotMakeExecution {
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT u.id FROM User u WHERE u.login = :login
""", Long.class).setParameter("login", login).uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl getUserIdByLogin: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getUserIdByLogin: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public Role getRoleByName(RoleTypes role){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT r FROM Role r WHERE r.name = :name
            """, Role.class)
                    .setParameter("name", role)
                    .uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl getRoleByName: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getRoleByName: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public User gtByIdFullVersion(Long userId){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.role
            WHERE u.id = :userId
            """, User.class)
                    .setParameter("userId", userId)
                    .uniqueResultOptional().orElse(null);
        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl gtByIdFullVersion: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl gtByIdFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<User> getUsersForModerator(UserForModeratorFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            List<Long> ids = getUsersIdsByFilter(filters);
            if (ids.isEmpty()) return List.of();

            List<User> res =  session.createQuery("""
            SELECT u FROM User u 
            LEFT JOIN FETCH u.role
            WHERE u.id IN (:ids)
            """, User.class)
                    .setParameter("ids", ids)
                    .getResultList();

            Map<Long, User> maps = res.stream().collect(Collectors.toMap(
                    User::getId, user -> user
            ));

            return ids.stream().map(maps::get).collect(Collectors.toList());
        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl getUsersForModerator: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getUsersForModerator: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<Long> getUsersIdsByFilter(UserForModeratorFilter filters){
        try{
            Session session = getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Long> query = builder.createQuery(Long.class);
            JpaRoot<User> root = query.from(User.class);

            List<JpaPredicate> predicates = buildPredicates(builder, root, filters);
            JpaOrder order = buildOrder(filters, builder, root);

            query.select(root.get("id"))
                    .where(predicates.toArray(new JpaPredicate[predicates.size()]))
                    .orderBy(order);
            return session.createQuery(query).getResultList();


        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl getUsersIdsByFilter: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getUsersIdsByFilter: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    private List<JpaPredicate>  buildPredicates(
            HibernateCriteriaBuilder builder,
            JpaRoot<User> root,
            UserForModeratorFilter filters
    ){
        List<JpaPredicate> predicates = new ArrayList<JpaPredicate>();
        if (filters.getLocked() != null ) {
            predicates.add(
                    builder.equal(root.get("nonLocked"), false)
            );
        }

        if (filters.getNonLocked() != null){
            predicates.add(
                    builder.equal(root.get("nonLocked"), true)
            );
        }

        if  (filters.getEndUpdatedDate()!= null){
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("updatedAt"),
                            DateTimeUtils.toInstantEndDay(filters.getEndUpdatedDate()))
            );
        }

        if (filters.getStartUpdatedDate()!= null){
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("updatedAt"),
                            DateTimeUtils.toInstant(filters.getStartUpdatedDate()))
            );
        }

        if  (filters.getEndCreatedDate()!= null){
            predicates.add(
                    builder.lessThanOrEqualTo(root.get("createdAt"),
                            DateTimeUtils.toInstantEndDay(filters.getEndCreatedDate()))
            );
        }

        if (filters.getStartCreatedDate()!= null){
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get("createdAt"),
                            DateTimeUtils.toInstant(filters.getStartCreatedDate()))
            );
        }

        if (filters.getCreatedAt() != null){
            predicates.add(
                    builder.between(root.get("createdAt"),
                            DateTimeUtils.toInstant(filters.getCreatedAt()),
                            DateTimeUtils.toInstantEndDay(filters.getCreatedAt()))
            );
        }

        if (filters.getUpdatedAt() != null){
            predicates.add(
                    builder.between(root.get("updatedAt"),
                            DateTimeUtils.toInstant(filters.getUpdatedAt()),
                            DateTimeUtils.toInstantEndDay(filters.getUpdatedAt()))
            );
        }

        if (filters.getRoleType() != null) {
            predicates.add(
                    builder.equal(root.get("role").get("name"), filters.getRoleType())
            );
        }else{
            predicates.add(
                    builder.or(
                            builder.equal(root.get("role").get("name"), RoleTypes.MIN_USER),
                            builder.equal(root.get("role").get("name"), RoleTypes.MAX_USER)
                    )
            );
        }

        return predicates;
    }

    private JpaOrder buildOrder(UserForModeratorFilter filters, HibernateCriteriaBuilder builder, JpaRoot<User> root){
        return switch (filters.getSortType()){
            case ASC -> builder.asc(root.get("id"));
            case DESC -> builder.desc(root.get("id"));
            case LOGIN_ASC -> builder.asc(root.get("login"));
            case LOGIN_DESC -> builder.desc(root.get("login"));
            case USERNAME_ASC -> builder.asc(root.get("username"));
            case USERNAME_DESC -> builder.desc(root.get("username"));
        };

    }

    @Transactional
    public List<ModeratorDto> getModerators(){
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
                SELECT u.id, u.username FROM User u WHERE u.role.name = :role
            """, ModeratorDto.class).setParameter("role", "MODERATOR").getResultList();
        }
        catch(HibernateException e){
            logger.error("Hibernate  UserHibImpl getModerators: " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception UserHibImpl getModerators: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }


}
