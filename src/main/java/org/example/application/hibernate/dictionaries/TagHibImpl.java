package org.example.application.hibernate.dictionaries;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.application.dto.TagDto;
import org.example.application.exceptions.CanNotMakeExecution;
import org.example.application.exceptions.DoesNoeExist;
import org.example.application.exceptions.NonHibernateException;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.hibernate.base_settings.HibernateAbstractDao;
import org.example.application.models.Tag;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@DependsOn("liquibase")
@Repository
public class TagHibImpl extends HibernateAbstractDao<Tag, Long, Logger> {
    private static final Logger logger =LogManager.getLogger(TagHibImpl.class);
    TagHibImpl() {
        super(Tag.class);
    }

    @Transactional
    public Tag update(TagDto dto) throws CanNotMakeExecution, NotCorrectInput {
        Session session = getSessionFactory().getCurrentSession();
        try {
            Tag old = session.get(Tag.class, dto.getId());
            if (old == null){
                throw new NotCorrectInput("Такого tag не существует");
            }

            if (dto.getName()!=null){
                old.setName(dto.getName());
            }
            //session.update(old); -> проверить работу
            return old;
        }
        catch(HibernateException e){
            logger.error("Hibernate Exception TagHibImpl update(TagDto): " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (NotCorrectInput e){
            throw new DoesNoeExist("Tag does not exist with given credentials");
        }
        catch (Exception e){
            logger.error("NonHibernate Exception TagHibImpl update(TagDto):: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<Tag> findAllById(List<Long> ids)throws CanNotMakeExecution, NotCorrectInput{
        try{
            Session session = getSessionFactory().getCurrentSession();
            return session.createQuery("""
            SELECT t FROM Tag t WHERE t.id In :ids
             """, Tag.class).setParameter("ids", ids).getResultList();
        }
        catch(HibernateException e){
            logger.error("Hibernate Exception TagHibImpl update(TagDto): " + e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception TagHibImpl update(TagDto):: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }


}
