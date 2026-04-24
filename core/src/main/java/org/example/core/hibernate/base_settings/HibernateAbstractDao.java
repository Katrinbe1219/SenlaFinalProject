package org.example.core.hibernate.base_settings;

//import jakarta.transaction.Transactional;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.NotCorrectInput;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

public class HibernateAbstractDao<T, ID extends Serializable, Logger extends org.apache.logging.log4j.Logger>
        implements HibernateGenericDao<T,ID, Logger>{

    @Autowired
    private SessionFactory sessionFactory;

    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private Class<T> entityClass;
    protected HibernateAbstractDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }


    @Override
    @Transactional(readOnly = true)
    public T findById(ID id, Logger logger) throws CanNotMakeExecution, NonHibernateException {
        try{
            return getSessionFactory().getCurrentSession().get(entityClass, id);
        } catch(HibernateException e){
            logger.error("Hibernate Проблема Abstract findById: "+ e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }catch(Exception e){
            logger.error("NonHibernate Проблема Abstract findById: "+ e.getMessage());
            throw  new  NonHibernateException(e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(Integer count, Integer page, Logger logger) {
        try {
            var query = getSessionFactory().getCurrentSession()
                    .createQuery("FROM " + getEntityClass().getSimpleName(), entityClass);

            if (count != null && page != null) {
                query.setFirstResult(count * page);
                query.setMaxResults(count);
            }

            return query.getResultList();
        } catch (HibernateException e) {
            logger.error("Hibernate Проблема Abstract findAll: "+ e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }
        catch(Exception e){
            logger.error("NonHibernate Проблема Abstract findAll: "+ e.getMessage());
            throw  new  NonHibernateException(e.getMessage());
        }

    }

    @Override
    @Transactional
    public void delete( ID id, Logger logger) {
        try {
            T found = findById(id, logger);
            if (found != null) {
                getSessionFactory().getCurrentSession().remove(found);
            }else{
                throw new NotCorrectInput("Object with given credentials does not exist");
            }
        }catch (HibernateException e){
            logger.error("Hibernate Проблема Abstract delete: "+ e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }
        catch (NotCorrectInput e){
            throw new DoesNoeExist("Object with given credentials does not exist");
        }
        catch(Exception e){
            logger.error("NonHibernate Проблема Abstract delete: "+ e.getMessage());
            throw  new  NonHibernateException(e.getMessage());
        }

    }

    @Override
    public T  save(T entity, Logger logger) {
        try {
            getSessionFactory().getCurrentSession().persist(entity);
            return entity;
        }
        catch (DataIntegrityViolationException e){
            logger.error("DataIntegrity Проблема Abstract save:"  + e.getMessage());
            throw  new DataIntegrityViolationException(e.getMessage());
        }
        catch (ConstraintViolationException e){
            throw new DataIntegrityViolationException(e.getMessage());
        }
        catch (HibernateException e){
            System.out.println("Class " + e.getClass());
            logger.error("Hibernate Проблема Abstract save: "+ e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }

        catch(Exception e){
            logger.error("NonHibernate Проблема Abstract save: "+ e.getMessage());
            throw  new  NonHibernateException(e.getMessage());
        }

    }

    @Override
    public T  update(T entity, Logger logger) {
        try {
            return getSessionFactory().getCurrentSession().merge(entity);

        }
        catch (DataIntegrityViolationException e){
            logger.error("DataIntegrity Проблема Abstract update:"  + e.getMessage());
            throw  new DataIntegrityViolationException(e.getMessage());
        }
        catch (ConstraintViolationException e){
            throw new DataIntegrityViolationException(e.getMessage());
        }
        catch (HibernateException e){
            logger.error("Hibernate Проблема Abstract update: "+ e.getMessage());
            throw  new CanNotMakeExecution(e.getMessage());
        }

        catch(Exception e){
            logger.error("NonHibernate Проблема Abstract update: "+ e.getMessage());
            throw  new  NonHibernateException(e.getMessage());
        }
    }
}
