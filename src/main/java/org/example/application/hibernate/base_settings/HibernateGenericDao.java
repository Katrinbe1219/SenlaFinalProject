package org.example.application.hibernate.base_settings;

import java.io.Serializable;
import java.util.List;

public interface HibernateGenericDao<T, ID extends Serializable, Logger> {
    T findById(ID id, Logger logger);
    List<T> findAll(Integer count, Integer page, Logger logger);
    void delete(ID id, Logger logger);
    T save(T entity, Logger logger);
    T  update(T entity, Logger logger);
}
