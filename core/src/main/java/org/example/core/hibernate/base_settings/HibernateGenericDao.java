package org.example.core.hibernate.base_settings;

import java.io.Serializable;
import java.util.List;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;

public interface HibernateGenericDao<T, ID extends Serializable, Logger> {
    T findById(ID id, Logger logger);
    List<T> findAll(Integer count, Integer page, Logger logger);
    List<T> findAllWithSort(Integer count, Integer page, BaseSortTypes filters, List<Long> ids, Logger logger);
    void delete(ID id, Logger logger);
    T save(T entity, Logger logger);
    T  update(T entity, Logger logger);
}
