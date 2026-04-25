package org.example.core.hibernate.documents;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.models.OutboxEvent;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

@Repository
public class OutboxEventHib extends HibernateAbstractDao<OutboxEvent, Long, Logger> {
    protected OutboxEventHib() {
        super(OutboxEvent.class);
    }
    private static final Logger logger = LogManager.getLogger(OutboxEventHib.class);

    @Transactional
    public void deleteById(Long id){
        try{
            Session session = getSessionFactory().getCurrentSession();
            session.createMutationQuery(
                            "DELETE FROM OutboxEvent WHERE id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
        }
        catch (HibernateException e){
            logger.error("OutboxEventHib delete " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception OutboxEventHib delete "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }


}
