package org.example.core.hibernate.dictionaries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.TagDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.models.Category;
import org.example.core.models.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TagHibImpl.class,
        IntegrationTestConfig.class
})
@Transactional // откат после каждого теста
public class TagHibImplTest {
    private static final Logger logger = LogManager.getLogger(TagHibImplTest.class);

    @Autowired
    TagHibImpl tagHib;

    private Tag createAndSave(String name) {
        Tag t = new Tag();
        t.setName(name);
        tagHib.save(t, logger);
        return t;
    }

    @Test
    @DisplayName("updateIfNotExist")
    @org.junit.jupiter.api.Tag("negative")
    void updateIfNotExist(){
        TagDto check = new TagDto();
        check.setId(1L);
        Exception ex = Assertions.assertThrows(DoesNoeExist.class, () -> tagHib.update(check));
        Assertions.assertEquals("Такого tag не существует", ex.getMessage());

    }

    @Test
    @DisplayName("updateIfNameIsTheSame")
    @org.junit.jupiter.api.Tag("negative")
    void updateIfNameIsTheSame(){
        Tag old = createAndSave("name");
        TagDto newOne = new TagDto();
        newOne.setId(old.getId());
        newOne.setName(old.getName());

        Exception ex = Assertions.assertThrows(NotCorrectInput.class, () -> tagHib.update(newOne));
        Assertions.assertEquals("Tag already has this name", ex.getMessage());

    }

    @Test
    @DisplayName("updateISuccessful")
    @org.junit.jupiter.api.Tag("positive")
    void updateISuccessful(){
        Tag old = createAndSave("name");
        TagDto newOne = new TagDto();
        newOne.setId(old.getId());
        newOne.setName("newName");

        Tag inserted = tagHib.update(newOne);
        Assertions.assertEquals(newOne.getName(), inserted.getName());
    }

    @Test
    @DisplayName("findAllByIdIfExist")
    @org.junit.jupiter.api.Tag("positive")
    void findAllByIdIfExist(){
        Tag first = createAndSave("name");
        Tag second = createAndSave("name1");
        Tag third = createAndSave("name2");

        List<Tag> tags = tagHib.findAllById(List.of(first.getId(), second.getId(), third.getId()));
        Assertions.assertEquals(3, tags.size());
        Assertions.assertTrue(tags.stream().map(Tag::getId).toList().contains(first.getId()));
    }

    @Test
    @DisplayName("findAllByIdIfNotExist")
    @org.junit.jupiter.api.Tag("positive")
    void findAllByIdIfNotExist(){

        List<Tag> tags = tagHib.findAllById(List.of(1L,2L,3L));
        Assertions.assertEquals(0, tags.size());
    }
}
