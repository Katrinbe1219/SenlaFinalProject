package org.example.hibernate;

//import jakarta.transaction.Transactional;
import org.apache.logging.log4j.Logger;
import org.example.exceptions.CanNotMakeExecution;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

public class HibernateAbstractDao<T, ID extends Serializable, Logger extends org.apache.logging.log4j.Logger> implements HibernateGenericDao<T,ID, Logger>{

    @Autowired
    private SessionFactory sessionFactory;

    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private Class<T> entityClass;
    HibernateAbstractDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }


    @Override
    @Transactional
    public T findById(ID id, Logger logger) throws CanNotMakeExecution {
        try{
            Session session= getSessionFactory().getCurrentSession();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<T> criteriaQuery = builder.createQuery(getEntityClass());
            JpaRoot<T> root = criteriaQuery.from(getEntityClass());

            criteriaQuery.select(root).where(builder.equal(root.get("id"), id));
            T result = session.createQuery(criteriaQuery).getSingleResult();
            return result;

        } catch(Exception e){
            logger.error("Проблема при получение обьекта по ID: "+ e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }
    }

    @Override
    public List<T> findAll(Logger logger) {
        return List.of();
    }

    @Override
    public void delete(T entity, Logger logger) {

    }

    @Override
    public void save(T entity, Logger logger) {

    }
}
