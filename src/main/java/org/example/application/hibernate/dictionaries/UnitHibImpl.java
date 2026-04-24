package org.example.application.hibernate.dictionaries;

import org.apache.logging.log4j.Logger;
import org.example.application.hibernate.base_settings.HibernateAbstractDao;
import org.example.application.models.Unit;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

@DependsOn("liquibase")
@Repository
public class UnitHibImpl extends HibernateAbstractDao<Unit, Long, Logger> {
    protected UnitHibImpl() {
        super(Unit.class);
    }
}
