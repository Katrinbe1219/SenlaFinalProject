package org.example.core.hibernate.dictionaries;

import org.apache.logging.log4j.Logger;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.models.Unit;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

@DependsOn("liquibase")
@Repository
public class UnitHibImpl extends HibernateAbstractDao<Unit, Long, Logger> {
    public UnitHibImpl() {
        super(Unit.class);
    }
}
