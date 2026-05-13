package org.example.core.hibernate.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.getting.rates.RateWithGoodNameDto;
import org.example.core.dto.getting.statistics.RecalculationForGoodDto;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.hibernate.base_settings.sorting_types.GoodSortType;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.models.Category;
import org.example.core.models.Good;
import org.example.core.models.Unit;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.ModeratorVerdict;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        CategoryHibImpl.class,
        GoodHibImpl.class,
        UnitHibImpl.class,
})
@Transactional
public class GoodHibImplTest {
    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    GoodHibImpl goodHib;
    @Autowired
    UnitHibImpl unitHib;
    @Autowired
    CategoryHibImpl categoryHib;

    private Unit unit;
    private Category category;

    private static final Logger logger = LogManager.getLogger(GoodHibImplTest.class);

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setShortName("short");
        unit.setFullName("fyll");
        unitHib.save(unit, logger);

        category = new Category();
        category.setName("TestCategory");
        categoryHib.save(category, logger);
        sessionFactory.getCurrentSession().flush();

    }

    private Good createGood(String name,
                            GoodStatusFromModerator status,
                            Double rate) {
        Good good = new Good();
        good.setName(name);
        good.setUnit(unit);
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        good.setModeratorStatus(status);
        good.setRate(rate);
        goodHib.save(good, logger);
        sessionFactory.getCurrentSession().flush();
        return good;
    }

    private Good createGoodWithCategory(String name, GoodStatusFromModerator status,
                                        Double rate, Category cat) {
        Good good = new Good();
        good.setName(name);
        good.setUnit(unit);
        good.setCategory(cat);
        good.setModeratorStatus(status);
        good.setRate(rate);
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        goodHib.save(good, logger);
        sessionFactory.getCurrentSession().flush();
        return good;
    }

    @Test
    @Tag("positive")
    @DisplayName("getAllIdsForRecalculationReturnsOnlyApproved")
    void getAllIdsForRecalculationReturnsOnlyApproved() {
        Good good = createGood("good", GoodStatusFromModerator.APPROVED, 4D);
        Good good1 = createGood("good1", GoodStatusFromModerator.SUSPICIOUS, 5D);
        Good good2 = createGood("good2", GoodStatusFromModerator.APPROVED, 2D);
        Good good3 = createGood("good3", GoodStatusFromModerator.APPROVED, 3D);

        List<RecalculationForGoodDto> dtos = goodHib.getAllIdsForRecalculation();
        Assertions.assertEquals(3, dtos.size());
        Assertions.assertFalse(dtos.stream().map(RecalculationForGoodDto::getGoodId).toList().contains(good1.getId()));

    }

    @Test
    @DisplayName("getAllIdsForRecalculationIfNothingFound")
    @Tag("positive")
    void getAllIdsForRecalculationIfNothingFound() {
        createGood("good", GoodStatusFromModerator.SUSPICIOUS, 4D);
        createGood("good1", GoodStatusFromModerator.SUSPICIOUS, 5D);
        createGood("good2", GoodStatusFromModerator.SUSPICIOUS, 2D);
        createGood("good3", GoodStatusFromModerator.SUSPICIOUS, 3D);

        List<RecalculationForGoodDto> dtos = goodHib.getAllIdsForRecalculation();
        Assertions.assertEquals(0, dtos.size());

    }

    @Test
    @Tag("positive")
    @DisplayName("getAllIdsForRecalculationReturnsRate")
    void getAllIdsForRecalculationReturnsRate() {
        Good good = createGood("Good", GoodStatusFromModerator.APPROVED, 4.0);

        List<RecalculationForGoodDto> result = goodHib.getAllIdsForRecalculation();

        result.stream()
                .filter(d -> d.getGoodId().equals(good.getId()))
                .findFirst()
                .ifPresent(d -> Assertions.assertEquals(4.0, d.getRate()));
    }

    @Test
    @Tag("positive")
    @DisplayName("findByIdFullVersionIfFound")
    void findByIdFullVersionIfFound() {
        Good good = createGoodWithCategory("FullGood", GoodStatusFromModerator.APPROVED, 3.0, category);

        Good result = goodHib.findByIdFullVersion(good.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals("FullGood", result.getName());
        Assertions.assertNotNull(result.getUnit());
        Assertions.assertNotNull(result.getCategory());
    }

    @Test
    @Tag("negative")
    @DisplayName("findByIdFullVersionIfNotFound")
    void findByIdFullVersionIfNotFound() {
        Good result = goodHib.findByIdFullVersion(99999L);
        Assertions.assertNull(result);
    }


    @Test
    @Tag("positive")
    @DisplayName("findAllByFiltersNoCriteria")
    void findAllByFiltersNoCriteria() {
        createGood("Good1", GoodStatusFromModerator.APPROVED, 3.0);
        createGood("Good2", GoodStatusFromModerator.APPROVED, 4.0);

        GoodFilter filters = new GoodFilter();
        List<Good> result = goodHib.findAllByFilters(filters, false);

        Assertions.assertEquals(2, result.size());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllByFiltersIsUserReturnsOnlyApproved")
    void findAllByFiltersIsUserReturnsOnlyApproved() {
        createGood("Approved", GoodStatusFromModerator.APPROVED, 3.0);
        createGood("Suspicious", GoodStatusFromModerator.SUSPICIOUS, 2.0);
        createGood("Approved2", GoodStatusFromModerator.APPROVED, 3.0);
        createGood("Approved3", GoodStatusFromModerator.APPROVED, 3.0);
        createGood("Suspicious1", GoodStatusFromModerator.SUSPICIOUS, 2.0);



        GoodFilter filters = new GoodFilter();
        filters.setStatus(GoodStatusFromModerator.APPROVED);

        List<Good> result = goodHib.findAllByFilters(filters, true);

        Assertions.assertTrue(result.stream()
                .allMatch(g -> g.getModeratorStatus() == GoodStatusFromModerator.APPROVED));
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllByFiltersFilterByCategoryIds")
    void findAllByFiltersFilterByCategoryIds() {
        Good withCat = createGoodWithCategory("WithCat", GoodStatusFromModerator.APPROVED, 3.0, category);
         createGoodWithCategory("WithCat1", GoodStatusFromModerator.SUSPICIOUS, 3.0, category);
         createGoodWithCategory("WithCat2", GoodStatusFromModerator.APPROVED, 3.0, category);

        createGood("NoCat", GoodStatusFromModerator.APPROVED, 3.0);

        GoodFilter filters = new GoodFilter();
        filters.setSortType(GoodSortType.ASC);
        filters.setCategoryIds(List.of(category.getId()));

        List<Good> result = goodHib.findAllByFilters(filters, false);
        Assertions.assertEquals(3, result.size());

        Assertions.assertTrue(result.stream().allMatch(g ->
                g.getCategory() != null && g.getCategory().getId().equals(category.getId())));
        Assertions.assertTrue(result.stream().anyMatch(g -> g.getId().equals(withCat.getId())));
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllByFiltersFilterByMinRating")
    void findAllByFiltersFilterByMinRating() {
        createGood("Low", GoodStatusFromModerator.APPROVED, 1.0);
        createGood("High", GoodStatusFromModerator.APPROVED, 4.0);
        createGood("High", GoodStatusFromModerator.APPROVED, 2.9);
        createGood("High", GoodStatusFromModerator.SUSPICIOUS, 3.0);
        createGood("High", GoodStatusFromModerator.SUSPICIOUS, 4.1);

        GoodFilter filters = new GoodFilter();
        filters.setSortType(GoodSortType.ASC);
        filters.setMinRating(3.0);

        List<Good> result = goodHib.findAllByFilters(filters, false);
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.stream().allMatch(g -> g.getRate() >= 3.0));
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllByFiltersFilterByMaxRating")
    void findAllByFiltersFilterByMaxRating() {
        createGood("Low", GoodStatusFromModerator.APPROVED, 1.0);
        createGood("High", GoodStatusFromModerator.APPROVED, 4.0);
        createGood("High", GoodStatusFromModerator.APPROVED, 2.3);
        createGood("High", GoodStatusFromModerator.APPROVED, 2.1);
        createGood("High", GoodStatusFromModerator.APPROVED, 0.9);

        GoodFilter filters = new GoodFilter();
        filters.setSortType(GoodSortType.ASC);
        filters.setMaxRating(2.0);

        List<Good> result = goodHib.findAllByFilters(filters, false);
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().allMatch(g -> g.getRate() <= 2.0));
    }


    @Test
    @Tag("positive")
    @DisplayName("findAllByFiltersWithPagination")
    void findAllByFiltersWithPagination() {
        createGood("Good1", GoodStatusFromModerator.APPROVED, 3.0);
        createGood("Good2", GoodStatusFromModerator.APPROVED, 3.0);
        createGood("Good3", GoodStatusFromModerator.APPROVED, 3.0);

        GoodFilter filters = new GoodFilter();
        filters.setSortType(GoodSortType.ASC);
        filters.setPage(0);
        filters.setSize(2);

        List<Good> result = goodHib.findAllByFilters(filters, false);

        Assertions.assertEquals(2, result.size());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllByFiltersEmptyResult")
    void findAllByFiltersEmptyResult() {
        createGood("Good1", GoodStatusFromModerator.APPROVED, 3.0);
        createGood("Good2", GoodStatusFromModerator.APPROVED, 3.0);

        GoodFilter filters = new GoodFilter();
        filters.setSortType(GoodSortType.ASC);
        filters.setMinRating(999.0);

        List<Good> result = goodHib.findAllByFilters(filters, false);
        Assertions.assertTrue(result.isEmpty());
    }


    @Test
    @Tag("positive")
    @DisplayName("updateStatusForManySetsSuspicious")
    void updateStatusForManySetsSuspicious() {
        Good good1 = createGood("Good1", GoodStatusFromModerator.APPROVED, 3.0);
        Good good2 = createGood("Good2", GoodStatusFromModerator.APPROVED, 3.0);

        Map<Long, ModeratorVerdict> updates = Map.of(
                good1.getId(), ModeratorVerdict.SUSPICIOUS,
                good2.getId(), ModeratorVerdict.SUSPICIOUS
        );

        goodHib.updateStatusForMany(updates);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Good updated1 = goodHib.findById(good1.getId(), logger);
        Good updated2 = goodHib.findById(good2.getId(), logger);

        Assertions.assertEquals(GoodStatusFromModerator.SUSPICIOUS, updated1.getModeratorStatus());
        Assertions.assertEquals(GoodStatusFromModerator.SUSPICIOUS, updated2.getModeratorStatus());
    }

    @Test
    @Tag("positive")
    @DisplayName("updateStatusForManyMixedVerdicts")
    void updateStatusForManyMixedVerdicts() {
        Good good1 = createGood("Good1", GoodStatusFromModerator.APPROVED, 3.0);
        Good good2 = createGood("Good2", GoodStatusFromModerator.SUSPICIOUS, 3.0);

        Map<Long, ModeratorVerdict> updates = Map.of(
                good1.getId(), ModeratorVerdict.SUSPICIOUS,
                good2.getId(), ModeratorVerdict.APPROVED
        );

        goodHib.updateStatusForMany(updates);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Good updated1 = goodHib.findById(good1.getId(), logger);
        Good updated2 = goodHib.findById(good2.getId(), logger);

        Assertions.assertEquals(GoodStatusFromModerator.SUSPICIOUS, updated1.getModeratorStatus());
        Assertions.assertEquals(GoodStatusFromModerator.APPROVED, updated2.getModeratorStatus());
    }


    @Test
    @Tag("positive")
    @DisplayName("findMaxRatesAmongAllWithoutSuspicious")
    void findMaxRatesAmongAllWithoutSuspicious() {
        createGood("Approved", GoodStatusFromModerator.APPROVED, 5.0);
        createGood("Suspicious", GoodStatusFromModerator.SUSPICIOUS, 4.9);

        List<RateWithGoodNameDto> result = goodHib.findMaxRatesAmongAll(10, false);

        Assertions.assertEquals(1, result.size());
    }

    @Test
    @Tag("positive")
    @DisplayName("findMaxRatesAmongAllWithSuspicious")
    void findMaxRatesAmongAllWithSuspicious() {
        createGood("Approved", GoodStatusFromModerator.APPROVED, 5.0);
        Good suspicious = createGood("Suspicious", GoodStatusFromModerator.SUSPICIOUS, 4.9);

        List<RateWithGoodNameDto> result = goodHib.findMaxRatesAmongAll(10, true);

        List<Long> ids = result.stream().map(RateWithGoodNameDto::getGoodId).toList();
        Assertions.assertTrue(ids.contains(suspicious.getId()));
    }

    @Test
    @Tag("positive")
    @DisplayName("findMaxRatesAmongAllLimitedByCount")
    void findMaxRatesAmongAllLimitedByCount() {
        createGood("Good1", GoodStatusFromModerator.APPROVED, 5.0);
        createGood("Good2", GoodStatusFromModerator.APPROVED, 4.0);
        createGood("Good3", GoodStatusFromModerator.APPROVED, 3.0);

        List<RateWithGoodNameDto> result = goodHib.findMaxRatesAmongAll(2, false);

        Assertions.assertEquals(2, result.size());
    }

    @Test
    @Tag("positive")
    @DisplayName("findMaxRatesAmongAllSortedByRateDesc")
    void findMaxRatesAmongAllSortedByRateDesc() {
        createGood("Low", GoodStatusFromModerator.APPROVED, 1.0);
        createGood("High", GoodStatusFromModerator.APPROVED, 5.0);
        createGood("Mid", GoodStatusFromModerator.APPROVED, 3.0);

        List<RateWithGoodNameDto> result = goodHib.findMaxRatesAmongAll(3, false);

        Assertions.assertTrue(result.get(0).getRate() >= result.get(1).getRate());
        Assertions.assertTrue(result.get(1).getRate() >= result.get(2).getRate());
    }
}
