package org.example.core.hibernate.dictionaries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.HibernateAbstractDao;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.models.Category;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@DependsOn("liquibase")
@Repository
public class CategoryHibImpl extends HibernateAbstractDao<Category, Long, Logger> {

    private static final Logger logger = LogManager.getLogger(CategoryHibImpl.class);
    CategoryHibImpl() {
        super(Category.class);
    }

    @Transactional
    public List<Long> findAllChildCategoryIds(List<Long> categoryIds){
        try{
            Session session = getSessionFactory().getCurrentSession();
            // до UNION ALL начальная часть, после рекурсивная
            // в рекурсии UNION убирающий дубли может быть сигналом для остановки для postgresql
            // distinct в конце все удалит (дубли)
            return session.createNativeQuery("""
                WITH RECURSIVE  category_tree AS (
    
       SELECT id FROM categories
              WHERE id  IN(:categoriesIds)
           UNION ALL
           SELECT c.id FROM categories c 
                    INNER JOIN category_tree ct ON ct.id = c.parent_id
   )
               SELECT distinct  id FROM category_tree
             """, Long.class)
                    .setParameter("categoriesIds", categoryIds).getResultList();
        }
        catch (HibernateException e){
            logger.error("Hibernate Exception CategoryHibImpl findAllChildCategoryIds: "+e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception CategoryHibImpl findAllChildCategoryIds: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public List<CategoryGetDto> findAllFullVersion(Integer count, Integer page,
                                                   BaseSortTypes filters, List<Long> ids) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try {

            StringBuilder sql = new StringBuilder(
                    "SELECT new org.example.core.dto.getting.categories.CategoryGetDto(\n" +
                    "            c.name,\n" +
                    "            c.id,\n" +
                    "            p.id,\n" +
                    "            p.name\n" +
                    "        )\n" +
                    "        FROM Category c\n" +
                    "        LEFT JOIN c.parent p");
            if (ids!=null){
                sql.append(" WHERE c.id IN (:ids) ");
            }

            sql.append(" Order BY c." + filters.getName() + " " + filters.getDir());

            var query = session.createQuery(sql.toString(), CategoryGetDto.class);
            if (page != null && count != null ){
                query.setFirstResult(count * page)
                        .setMaxResults(count);
            }

            if (ids!=null){
                query.setParameter("ids", ids);
            }

           return query.getResultList();

        }
        catch (HibernateException e){
            logger.error("Hibernate Exception CategoryHibImpl findAllFullVersion: "+e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception CategoryHibImpl findAllFullVersion: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<Category> getMainCategories() throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try {
            return session.createQuery(
                    "SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.parent " +
                    "WHERE c.parent is null", Category.class).list();
        }
        catch (HibernateException e){
            logger.error("Hibernate Exception CategoryHibImpl getMainCategories: "+e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception CategoryHibImpl getMainCategories: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    public List<Category> getSubCategoriesByMainFullVersion(Long mainId) throws CanNotMakeExecution {
        Session session = getSessionFactory().getCurrentSession();
        try {
            return session.createQuery(
                    "SELECT DISTINCT c FROM Category c" +
                            " LEFT JOIN FETCH  c.parent " +
                            " WHERE c.parent = :id"  , Category.class
            )
                    .setParameter("parent", mainId).list();
        }
        catch (HibernateException e){
            logger.error("Hibernate Exception CategoryHibImpl getSubCategoriesByMain: "+e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception CategoryHibImpl getSubCategoriesByMain: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }

    @Transactional
    public Category findByNameFullVersion(String name) throws  CanNotMakeExecution, NonHibernateException{
        Session session = getSessionFactory().getCurrentSession();
        try {
            return  session.createQuery(
                    "SELECT DISTINCT c FROM Category c " +
                            "LEFT JOIN FETCH c.parent " +
                            "WHERE c.name = :name" , Category.class
            ).setParameter("name", name).uniqueResultOptional().orElse(null);
        }
        catch (HibernateException e){
            logger.error("Hibernate Exception CategoryHibImpl findByName: "+e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (Exception e){
            logger.error("NonHibernate Exception CategoryHibImpl findByName: "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    @Transactional
    // возвращает новый объект
    public Category update(Category old ) throws CanNotMakeExecution, NotCorrectInput {
        Session session = getSessionFactory().getCurrentSession();
        try {

            Category category = session.get(Category.class, old.getId());
            if (category == null){
                throw new DoesNoeExist("Категории с таким id не существует");
            }

            if(category.getName().equalsIgnoreCase(old.getName())){
                throw new NotCorrectInput("Category already has this name");
            }
            if (old.getName() != null){
                category.setName(old.getName());
            }
            category.setParent(old.getParent());

            return category;
        }
        catch (HibernateException e){
            logger.error("Hibernate Exception CategoryHibImpl update(CategoryDto): "+e.getMessage());
            throw new CanNotMakeExecution(e.getMessage());
        }
        catch (NotCorrectInput e){
            throw e;
        }
        catch (DoesNoeExist e){
            throw new DoesNoeExist("Category with id: " + old.getId() + " does not exist");
        }
        catch (Exception e){

            logger.error("NonHibernate Exception CategoryHibImpl update(CategoryDto): "+e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }

    }


}
