package org.example.core.hibernate.documents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.getting.rates.RateFullDto;
import org.example.core.dto.getting.rates.RateValidGoodDto;
import org.example.core.hibernate.TestDataHelper;
import org.example.core.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.GoodHibImplTest;
import org.example.core.models.Category;
import org.example.core.models.Good;
import org.example.core.models.Unit;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.RatingStatus;
import org.example.core.models.types.RatingTriggerType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        RateHibImpl.class,
        GoodHibImpl.class,
        UnitHibImpl.class,
        TestDataHelper.class
})
public class RateHibImplTest {
    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    TestDataHelper helper;

    @Autowired
    GoodHibImpl goodHib;
    @Autowired
    UnitHibImpl unitHib;
    @Autowired
    RateHibImpl rateHib;

    private Unit unit;
    private Good basicGood;

    private static final Logger logger = LogManager.getLogger(RateHibImplTest.class);

    @BeforeEach
    void setUp() {
        unit = helper.createUnit();
        basicGood =helper.createGood("default", 5d, unit);

    }

    @AfterEach
    void tearDown() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.createNativeQuery("DELETE FROM rating_recalculation_log").executeUpdate();
        session.createNativeQuery("DELETE FROM goods").executeUpdate();
        session.createNativeQuery("DELETE FROM units").executeUpdate();
        tx.commit();
        session.close();
    }



    @Test
    @DisplayName("saveLogAndGetRatesByFilterGoodId")
    void saveLogAndGetRatesByFilterGoodId(){

        List<RateFullDto> found = rateHib.getRatesByFilter(new RatingRecalcFilter());

        Assertions.assertEquals(0, found.size());

        rateHib.saveLog(basicGood, null, RatingStatus.SUCCESS,
                RatingTriggerType.SCHEDULED, 1D, 2D);

        RatingRecalcFilter filters = new RatingRecalcFilter();
        filters.setGoodId(basicGood.getId());

        found = rateHib.getRatesByFilter(filters);

        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(basicGood.getName(), found.get(0).getGoodName());

    }

    @Test
    @DisplayName("saveLogsWithNoOldRatesAndGetRatesByFilter")
    void saveLogsWithNoOldRatesAndGetRatesByFilter(){

        List<RateFullDto> found = rateHib.getRatesByFilter(new RatingRecalcFilter());
        Assertions.assertEquals(0, found.size());

        Good good = helper.createGood("apple", 4D, unit);

        rateHib.saveLogs(Map.of(basicGood.getId(), basicGood, good.getId(), good),
                null, RatingStatus.FAILED,
                RatingTriggerType.SCHEDULED,
                Map.of());

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        List result = session.createNativeQuery(
                "SELECT * FROM rating_recalculation_log"
        ).getResultList();
        System.out.println("В БД записей: " + result.size());
        tx.commit();
        session.close();
        found = rateHib.getRatesByFilter(new RatingRecalcFilter());

        Assertions.assertEquals(2, found.size());
        Assertions.assertTrue(found.stream().map(RateFullDto::getGoodName).toList().contains(good.getName()));
        Assertions.assertTrue(found.stream().map(RateFullDto::getGoodName).toList().contains(basicGood.getName()));
        Assertions.assertEquals(1,
                found.stream().collect(Collectors.toMap(
                        RateFullDto::getRatingStatus,
                        RateFullDto::getId,
                        (old, newOne)-> old
                )).keySet().size()
        );
    }

    @Test
    @DisplayName("saveErrorsWithGetRatesByFilter")
    void saveErrorsWithGetRatesByFilter(){
        List<RateFullDto> found = rateHib.getRatesByFilter(new RatingRecalcFilter());
        Assertions.assertEquals(0, found.size());

        Good good = helper.createGood("apple", 4D, unit);
        rateHib.saveErrors(Map.of(basicGood.getId(), basicGood, good.getId(), good),
                "errorMessage",
                RatingStatus.FAILED, RatingTriggerType.SCHEDULED);

        found = rateHib.getRatesByFilter(new RatingRecalcFilter());
        Assertions.assertEquals(2, found.size());
        Assertions.assertTrue(found.stream().map(RateFullDto::getGoodName).toList().contains(good.getName()));
        Assertions.assertTrue(found.stream().map(RateFullDto::getGoodName).toList().contains(basicGood.getName()));
    }

    @Test
    @DisplayName("findValidMaxRatesAmongProductIfNotFound")
    void findValidMaxRatesAmongProductIfNotFound(){
        List<RateValidGoodDto> found = rateHib.findValidMaxRatesAmongProduct(15,1L);
        Assertions.assertEquals(0, found.size());
    }

    @Test
    @DisplayName("findValidMaxRatesAmongProductIfFound")
    void findValidMaxRatesAmongProductIfFound(){

        rateHib.saveLog(
                basicGood,
                null,
                RatingStatus.FIRST_ADDED,
                RatingTriggerType.SCHEDULED,
                basicGood.getRate(), 4D);
        rateHib.saveLog(
                basicGood,
                "testing",
                RatingStatus.FAILED,
                RatingTriggerType.SCHEDULED,
                4D, null);

        rateHib.saveLog(
                basicGood,
                null,
                RatingStatus.SUCCESS,
                RatingTriggerType.SCHEDULED,
                4D, 3D);
        rateHib.saveLog(
                basicGood,
                "null",
                RatingStatus.SUCCESS,
                RatingTriggerType.SCHEDULED,
                3D, 5D);

        List<RateValidGoodDto> found = rateHib.findValidMaxRatesAmongProduct(10,basicGood.getId());
        Assertions.assertEquals(3, found.size());
        Assertions.assertEquals(5D, found.get(0).getRate());
        Assertions.assertEquals(4D, found.get(1).getRate());
    }



}
