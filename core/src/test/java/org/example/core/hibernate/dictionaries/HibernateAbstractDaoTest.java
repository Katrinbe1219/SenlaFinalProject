package org.example.core.hibernate.dictionaries;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.models.District;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        DistrictHibImpl.class,
        IntegrationTestConfig.class
})

@Transactional // откат после каждого теста
public class HibernateAbstractDaoTest {
    private static final Logger logger = LogManager.getLogger(HibernateAbstractDaoTest.class);

    @Autowired
    private DistrictHibImpl districtHib;

    private District createAndSave(String name){
        District district = new District();
        district.setName(name);
        districtHib.save(district, logger);
        return district;
    }


    @Test
    @Tag("positive")
    @DisplayName("findByIdIfExists")
    void findByIdIfExists(){
        District dis= createAndSave("vorosh");
        District found = districtHib.findById(dis.getId(), logger);
        Assertions.assertNotNull(found);
        Assertions.assertEquals(dis.getId(), found.getId());
    }

    @Test
    @Tag("positive")
    @DisplayName("findByIdIfNotExists")
    void findByIdIfNotExists(){
        District dis= new District();
        dis.setId(1L);

        District found = districtHib.findById(dis.getId(), logger);
        Assertions.assertNull(found);
    }

    @Test
    @Tag("positive")
    @DisplayName("findByAllIfNoPagination")
    void findByAllIfNoPagination(){
        District dis= createAndSave("first");
        District dis1 = createAndSave("second");
        District dis2 = createAndSave("third");

        List<District> found = districtHib.findAll(null,null, logger);
        Assertions.assertEquals(3, found.size());
        Assertions.assertTrue(found.stream().map(District::getId).toList().contains(dis.getId()));
        Assertions.assertTrue(found.stream().map(District::getId).toList().contains(dis1.getId()));
        Assertions.assertTrue(found.stream().map(District::getId).toList().contains(dis2.getId()));
    }

    @Test
    @Tag("positive")
    @DisplayName("findByAllIfNoPagination")
    void findByAllIfPagination(){
        createAndSave("first");
       createAndSave("second");
        createAndSave("third");

        List<District> found = districtHib.findAll(2,0, logger);
        Assertions.assertEquals(2, found.size());
        Assertions.assertEquals(2, found.stream().map(District::getId).toList().stream().distinct().count());
    }

    @Test
    @Tag("positive")
    @DisplayName("saveAndFindById")
    void saveAndFindById() {
        District saved = createAndSave("TestDistrict");

        District found = districtHib.findById(saved.getId(), logger);

        Assertions.assertNotNull(found);
        Assertions.assertEquals("TestDistrict", found.getName());
    }

    @Test
    @Tag("negative")
    @DisplayName("saveIfDuplicateName")
    void saveIfDuplicateName() {
        createAndSave("Duplicate");

        Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> createAndSave("Duplicate")
        );
    }

    @Test
    @Tag("positive")
    @DisplayName("deleteIfSuccessful")
    void deleteIfSuccessful() {
        District saved = createAndSave("ToDelete");

        districtHib.delete(saved.getId(), logger);

        District found = districtHib.findById(saved.getId(), logger);
        Assertions.assertNull(found);
    }

    @Test
    @Tag("negative")
    @DisplayName("deleteIfNotFound")
    void deleteIfNotFound() {
        Assertions.assertThrows(
                DoesNoeExist.class,
                () -> districtHib.delete(99999L, logger)
        );
    }

    @Test
    @Tag("positive")
    @DisplayName("updateIfSuccessful")
    void updateIfSuccessful() {
        District saved = createAndSave("OldName");
        saved.setName("NewName");

        districtHib.update(saved, logger);

        District found = districtHib.findById(saved.getId(), logger);
        Assertions.assertEquals("NewName", found.getName());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllWithSort")
    void findAllWithSortIfWSortAndNoPagination() {
        District first = createAndSave("first");
        District sec =createAndSave("second");
        createAndSave("third");

        List<District> found = districtHib.findAllWithSort(null,null, BaseSortTypes.NAME_ASC, List.of(), logger);

        Assertions.assertEquals(3, found.size());
        Assertions.assertEquals(first.getId(), found.get(0).getId().intValue());
        Assertions.assertEquals(sec.getId(), found.get(1).getId().intValue());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllWithSort")
    void findAllWithSortIfSortAndPagination() {
        createAndSave("first");
        createAndSave("second");
        District third = createAndSave("third");

        List<District> found = districtHib.findAllWithSort(1,0, BaseSortTypes.DESC, List.of(), logger);
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(third.getId(), found.get(0).getId().intValue());
    }


}
