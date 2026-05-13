package org.example.core.hibernate.dictionaries;

import org.apache.logging.log4j.Logger;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.models.District;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

@DependsOn("liquibase")
@Repository
public class DistrictHibImpl extends HibernateAbstractDao<District, Long, Logger> {

    DistrictHibImpl() {
        super(District.class);
    }

}
