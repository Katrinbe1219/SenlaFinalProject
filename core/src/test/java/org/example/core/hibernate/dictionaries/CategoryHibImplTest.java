package org.example.core.hibernate.dictionaries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.models.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CategoryHibImpl.class,
        IntegrationTestConfig.class
})
@Transactional // откат после каждого теста
public class CategoryHibImplTest {
    private static final Logger logger = LogManager.getLogger(CategoryHibImplTest.class);
    @Autowired
    CategoryHibImpl categoryHib;

    private Category createAndSave(String name, Category parent) {
        Category c = new Category();
        c.setName(name);
        c.setParent(parent);
        categoryHib.save(c, logger);
        return c;
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllChildCategoryIdsReturnsChildrenRecursively")
    void findAllChildCategoryIdsReturnsChildrenRecursively(){
        Category parent = createAndSave("Parent", null);
        Category child = createAndSave("Child", parent);
        Category grandChild = createAndSave("GrandChild", parent);
        List<Long> res = categoryHib.findAllChildCategoryIds(List.of(
                parent.getId()
        ));

        Assertions.assertTrue(res.contains(child.getId()));
        Assertions.assertTrue(res.contains(grandChild.getId()));
        Assertions.assertTrue(res.contains(parent.getId()));
        // нет дубликатов
        Assertions.assertEquals(res.size(), res.stream().distinct().count());

    }

    @Test
    @Tag("positive")
    @DisplayName("findAllChildCategoryIdsIfNoChildren")
    void findAllChildCategoryIdsIfNoChildren() {
        Category category = createAndSave("Alone", null);

        List<Long> result = categoryHib.findAllChildCategoryIds(List.of(category.getId()));

        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.contains(category.getId()));
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllFullVersionIfSorting")
    void findAllFullVersionIfSorting(){
        Category parent = createAndSave("Parent", null);
        createAndSave("child11", parent);
        Category parent1  =createAndSave("parent2", null);
        createAndSave("child21", parent1);
        Category last = createAndSave("no", null);



        List<CategoryGetDto> res = categoryHib.findAllFullVersion(
               null, null, BaseSortTypes.DESC, List.of()
        );
        Assertions.assertEquals(5, res.size());
        Assertions.assertEquals(res.get(0).getId(), last.getId());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllFullVersionIfPagination")
    void findAllFullVersionIfPagination(){
        Category parent = createAndSave("Parent", null);
        createAndSave("child11", parent);
        Category parent1  =createAndSave("parent2", null);
        createAndSave("child21", parent1);
        Category last = createAndSave("no", null);



        List<CategoryGetDto> res = categoryHib.findAllFullVersion(
                3, 0, BaseSortTypes.DESC, List.of()
        );
        Assertions.assertEquals(3, res.size());
        Assertions.assertEquals(res.get(2).getId(), parent1.getId());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllFullVersionIfPagination")
    void findAllFullVersionIfIds(){
        Category parent = createAndSave("Parent", null);
        createAndSave("child11", parent);
        Category parent1  =createAndSave("parent2", null);
        createAndSave("child21", parent1);
        Category last = createAndSave("no", null);



        List<CategoryGetDto> res = categoryHib.findAllFullVersion(
                2, 0, BaseSortTypes.DESC, List.of(parent.getId(), last.getId())
        );
        Assertions.assertEquals(2, res.size());
        Assertions.assertTrue(res.stream().map(CategoryGetDto::getId).toList().contains(parent.getId()));
        Assertions.assertTrue(res.stream().map(CategoryGetDto::getId).toList().contains(last.getId()));
    }

    @Tag("negative")
    @Test
    @DisplayName("updateIfDoesNotExist")
    public void updateIfDoesNotExist(){
        Category old = new Category();
        old.setId(1L);

        Exception ex = Assertions.assertThrows(DoesNoeExist.class, ()->
                categoryHib.update(old));
        Assertions.assertEquals("Категории с таким id не существует", ex.getMessage());
    }

    @Tag("negative")
    @Test
    @DisplayName("updateIfNameIsEqual")
    public void updateIfNameIsEqual(){
        Category old = createAndSave("name", null);

        Category newOne= new Category();
        newOne.setId(old.getId());
        newOne.setName("name");

        Exception ex = Assertions.assertThrows(NotCorrectInput.class, ()->
                categoryHib.update(newOne));
        Assertions.assertEquals("Category already has this name", ex.getMessage());
    }
    @Tag("negative")
    @Test
    @DisplayName("updateIfSuccessful")
    public void updateIfSuccessful(){
        Category old = createAndSave("name", null);

        Category newOne= new Category();
        newOne.setId(old.getId());
        newOne.setName("newName");

        Assertions.assertEquals("newName", categoryHib.update(newOne).getName());
         }




}
