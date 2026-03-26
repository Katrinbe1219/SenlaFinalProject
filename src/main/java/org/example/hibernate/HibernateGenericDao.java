package org.example.hibernate;

import java.io.Serializable;
import java.util.List;

public interface HibernateGenericDao<T, ID extends Serializable, Logger> {
    T findById(ID id, Logger logger);
    List<T> findAll(Logger logger);
    void delete(T entity, Logger logger);
    void save(T entity, Logger logger);
}
