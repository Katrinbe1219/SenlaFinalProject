package org.example.core.hibernate.documents;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.creating.ManyModeratorLogCreateDto;
import org.example.core.dto.creating.ModeratorLogCreateDto;
import org.example.core.hibernate.base_settings.filters.ModeratorRecalcFilter;
import org.example.core.hibernate.base_settings.sorting_types.ModeratorRecalcSortType;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.models.*;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.ModeratorVerdict;
import org.example.core.models.types.RoleTypes;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        ModeratorRecalcHib.class,
        UserHibImpl.class,
        GoodHibImpl.class,
        UnitHibImpl.class,
})
@Transactional
public class ModeratorRecalcHibTest {
    private static final Logger logger = LogManager.getLogger(ModeratorRecalcHibTest.class);

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    ModeratorRecalcHib moderatorRecalcHib;

    @Autowired
    UserHibImpl userHib;

    @Autowired
    GoodHibImpl goodHib;

    @Autowired
    UnitHibImpl unitHib;

    private Unit unit;

    private Good createGood(String name){
        Good good = new Good();
        good.setUnit(unit);
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        good.setName(name);
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        good.setRate(1d);
        goodHib.save(good, logger);
        return good;
    }

    private User createUser(String login, String username){
        sessionFactory.getCurrentSession()
                .createNativeQuery("INSERT INTO roles (name) VALUES('MIN_USER') ON CONFLICT DO NOTHING")
                .executeUpdate();

        Role role =  sessionFactory.getCurrentSession().createQuery("FROM Role WHERE name = :name", Role.class)
                .setParameter("name", RoleTypes.MIN_USER).uniqueResult();


        User user = new User();
        user.setLogin(login);
        user.setUsername(username);
        user.setUpdatedAt(Instant.now());
        user.setCreatedAt(Instant.now());
        user.setPassword("pas");
        user.setNonLocked(true);
        user.setRole(role);
        userHib.save(user, logger);
        return user;
    }

    @BeforeEach
    void setUp(){
        unit = new Unit();
        unit.setFullName("full");
        unit.setShortName("short");
        unitHib.save(unit, logger);
    }

    @Tag("positive")
    @DisplayName("createManyLogsAndCheckByFindAllFullVersion")
    @Test
    void createManyLogsAndCheckByFindAllFullVersion(){
        Good good = createGood("good1");
        Good good1 = createGood("good2");
        User user = createUser("admin", "ad123");

        ModeratorLogCreateDto  dto = new ModeratorLogCreateDto();
        dto.setGoodId(good.getId());
        dto.setComment("comment");
        dto.setVerdict(ModeratorVerdict.APPROVED);

        ModeratorLogCreateDto  dto1 = new ModeratorLogCreateDto();
        dto1.setVerdict(ModeratorVerdict.SUSPICIOUS);
        dto1.setGoodId(good1.getId());
        dto1.setComment("comment another");

        ManyModeratorLogCreateDto fullDto = new ManyModeratorLogCreateDto();
        fullDto.setVerdicts(List.of(dto, dto1));

        moderatorRecalcHib.createManyLogs(fullDto, user);

        ModeratorRecalcFilter filters=  new ModeratorRecalcFilter();
        filters.setModeratorId(user.getId());

        List<ModeratorRatingCheck> found = moderatorRecalcHib.findAllFullVersion(filters);
        Assertions.assertEquals(2, found.size());
        Assertions.assertTrue(found.stream().map(r -> r.getGood().getId()).toList().contains(good.getId()));
        Assertions.assertTrue(found.stream().map(r -> r.getGood().getId()).toList().contains(good1.getId()));


    }



    @Test
    @DisplayName("getModeratorRatingChecksByGoodIdsIfGoodExist")
    void getModeratorRatingChecksByGoodIdsIfGoodExist(){
        // create with createManyLog
        Good good = createGood("good1");
        Good good1 = createGood("good2");

        User user = createUser("admin", "ad123");

        ModeratorLogCreateDto  dto = new ModeratorLogCreateDto();
        dto.setGoodId(good.getId());
        dto.setComment("comment");
        dto.setVerdict(ModeratorVerdict.APPROVED);

        ModeratorLogCreateDto  dto1 = new ModeratorLogCreateDto();
        dto1.setVerdict(ModeratorVerdict.SUSPICIOUS);
        dto1.setGoodId(good1.getId());
        dto1.setComment("comment another");

        ModeratorLogCreateDto  dto2 = new ModeratorLogCreateDto();
        dto2.setGoodId(good.getId());
        dto2.setComment("comment");
        dto2.setVerdict(ModeratorVerdict.SUSPICIOUS);

        ManyModeratorLogCreateDto fullDto = new ManyModeratorLogCreateDto();
        fullDto.setVerdicts(List.of(dto, dto1, dto2));

        moderatorRecalcHib.createManyLogs(fullDto, user);

        // check getModeratorRatingChecksByGoodIds
        List<ModeratorRatingCheck> found = moderatorRecalcHib.getModeratorRatingChecksByGoodIds(Set.of(good.getId()));
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(good.getId(), found.get(0).getGood().getId());



    }

    @Test
    @DisplayName("getModeratorRatingChecksByGoodIdsIfGoodExist")
    void getModeratorRatingChecksByGoodIdsIfGoodDoesNotExist(){
        // create with createManyLog
        Good good = createGood("good1");
        Good good1 = createGood("good2");

        User user = createUser("admin", "ad123");

        ModeratorLogCreateDto  dto = new ModeratorLogCreateDto();
        dto.setGoodId(good.getId());
        dto.setComment("comment");
        dto.setVerdict(ModeratorVerdict.APPROVED);


        ManyModeratorLogCreateDto fullDto = new ManyModeratorLogCreateDto();
        fullDto.setVerdicts(List.of(dto));

        moderatorRecalcHib.createManyLogs(fullDto, user);

        // check getModeratorRatingChecksByGoodIds
        List<ModeratorRatingCheck> found = moderatorRecalcHib.getModeratorRatingChecksByGoodIds(Set.of(20000L));
        Assertions.assertEquals(0, found.size());

    }

    @Test
    @DisplayName("")
    void findAllFullVersionByFilters(){
        // create with createManyLog
        Good good = createGood("good1");
        Good good1 = createGood("good2");

        User user = createUser("admin", "ad123");
        User user1 = createUser("admin1", "ad3");

        ModeratorLogCreateDto  dto = new ModeratorLogCreateDto();
        dto.setGoodId(good.getId());
        dto.setComment("comment");
        dto.setVerdict(ModeratorVerdict.APPROVED);

        ModeratorLogCreateDto  dto1 = new ModeratorLogCreateDto();
        dto1.setVerdict(ModeratorVerdict.SUSPICIOUS);
        dto1.setGoodId(good1.getId());
        dto1.setComment("comment another");

        ModeratorLogCreateDto  dto2 = new ModeratorLogCreateDto();
        dto2.setGoodId(good.getId());
        dto2.setComment("comment");
        dto2.setVerdict(ModeratorVerdict.SUSPICIOUS);

        ManyModeratorLogCreateDto fullDto = new ManyModeratorLogCreateDto();
        fullDto.setVerdicts(List.of(dto, dto1));

        moderatorRecalcHib.createManyLogs(fullDto, user);

        fullDto.setVerdicts(List.of(dto2));
        moderatorRecalcHib.createManyLogs(fullDto, user1);



        // ----------------
        ModeratorRecalcFilter filters = new ModeratorRecalcFilter();
        filters.setSortType(ModeratorRecalcSortType.GOOD_ID_DESC);
        filters.setModeratorId(user.getId());
        filters.setCount(1);

        List<ModeratorRatingCheck> found = moderatorRecalcHib.findAllFullVersion(filters);
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(good1.getId(), found.get(0).getGood().getId());

    }

    @Test
    @DisplayName("")
    void findAllFullVersionWithNoFilters(){
        // create with createManyLog
        Good good = createGood("good1");
        Good good1 = createGood("good2");

        User user = createUser("admin", "ad123");
        User user1 = createUser("admin1", "ad3");

        ModeratorLogCreateDto  dto = new ModeratorLogCreateDto();
        dto.setGoodId(good.getId());
        dto.setComment("comment");
        dto.setVerdict(ModeratorVerdict.APPROVED);

        ModeratorLogCreateDto  dto1 = new ModeratorLogCreateDto();
        dto1.setVerdict(ModeratorVerdict.SUSPICIOUS);
        dto1.setGoodId(good1.getId());
        dto1.setComment("comment another");

        ModeratorLogCreateDto  dto2 = new ModeratorLogCreateDto();
        dto2.setGoodId(good.getId());
        dto2.setComment("comment");
        dto2.setVerdict(ModeratorVerdict.SUSPICIOUS);

        ManyModeratorLogCreateDto fullDto = new ManyModeratorLogCreateDto();
        fullDto.setVerdicts(List.of(dto, dto1));

        moderatorRecalcHib.createManyLogs(fullDto, user);

        fullDto.setVerdicts(List.of(dto2));
        moderatorRecalcHib.createManyLogs(fullDto, user1);



        // ----------------
        ModeratorRecalcFilter filters = new ModeratorRecalcFilter();


        List<ModeratorRatingCheck> found = moderatorRecalcHib.findAllFullVersion(filters);
        Assertions.assertEquals(3, found.size());
    }
}
