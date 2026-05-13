package org.example.core.hibernate.documents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.dto.creating.ReviewCreateDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.TestDataHelper;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewAdvancedFilters;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewForUserFilters;
import org.example.core.hibernate.base_settings.sorting_types.ReviewSortTypes;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.models.*;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.RoleTypes;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        ReviewHibImpl.class,
        GoodHibImpl.class,
        UnitHibImpl.class,
        UserHibImpl.class,
})
@Transactional
class ReviewHibImplTest {

    @Autowired
    ReviewHibImpl reviewHib;

    @Autowired
    SessionFactory sessionFactory;
    @Autowired
    GoodHibImpl goodHib;
    @Autowired
    UnitHibImpl unitHib;

    private static final Logger logger = LogManager.getLogger(ReviewHibImplTest.class);

    private Unit unit;
    private Good good;
    private User user;
    private User moderator;
    private Role role;

    @Autowired
    private UserHibImpl userHibImpl;

    private User createUser(String login, String username){
        User user2 = new User();
        user2.setLogin(login);
        user2.setUsername(username);
        user2.setPassword("pass");
        user2.setRole(user.getRole());
        user2.setNonLocked(true);
        user2.setCreatedAt(Instant.now());
        user2.setUpdatedAt(Instant.now());
        user2.setRole(role);

        userHibImpl.save(user2, logger);
        return user2;
    }

    public Good createGood(String name, Double rate, Unit unit) {
        Good good = new Good();
        good.setName(name);
        good.setUnit(unit);
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        good.setRate(rate);
        goodHib.save(good, logger);
        return good;
    }

    public Unit createUnit() {
        Unit unit = new Unit();
        unit.setShortName("short");
        unit.setFullName("full");
        unitHib.save(unit, logger);
        return unit;
    }

    @BeforeEach
    void setUp() {
        unit = createUnit();
        good = createGood("TestGood", 4.0, unit);

        sessionFactory.getCurrentSession()
                .createNativeQuery("INSERT INTO roles (name) VALUES('MIN_USER') ON CONFLICT DO NOTHING")
                .executeUpdate();
        sessionFactory.getCurrentSession()
                .createNativeQuery("INSERT INTO roles (name) VALUES('MODERATOR') ON CONFLICT DO NOTHING")
                .executeUpdate();

       role = sessionFactory.getCurrentSession()
                .createQuery("FROM Role WHERE name = :name", Role.class)
                .setParameter("name", RoleTypes.MIN_USER).uniqueResult();
        Role modRole = sessionFactory.getCurrentSession()
                .createQuery("FROM Role WHERE name = :name", Role.class)
                .setParameter("name", RoleTypes.MODERATOR).uniqueResult();

        user = new User();
        user.setLogin("userlogin");
        user.setUsername("username");
        user.setPassword("pass");
        user.setRole(role);
        user.setNonLocked(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        userHibImpl.save(user, logger);

        moderator = new User();
        moderator.setLogin("modlogin");
        moderator.setUsername("modusername");
        moderator.setPassword("pass");
        moderator.setRole(modRole);
        moderator.setNonLocked(true);
        moderator.setCreatedAt(Instant.now());
        moderator.setUpdatedAt(Instant.now());
        userHibImpl.save(moderator, logger);
    }

    private Review createReview(User u, Good g, int rate) {
        ReviewCreateDto dto = new ReviewCreateDto();
        dto.setRate(rate);
        dto.setReview("review text");
        Review review = reviewHib.createReview(dto, g, u);
        sessionFactory.getCurrentSession().flush();
        return review;
    }


    @Test
    @DisplayName("createReviewIfSuccessful")
    void createReviewIfSuccessful() {
        Review review = createReview(user, good, 4);

        Assertions.assertNotNull(review.getId());
        Assertions.assertEquals(4, review.getRate());
        Assertions.assertEquals(user.getId(), review.getUser().getId());
        Assertions.assertEquals(good.getId(), review.getGood().getId());
    }

    @Test
    @DisplayName("createReviewIfDuplicate")
    void createReviewIfDuplicate() {
        createReview(user, good, 4);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> createReview(user, good, 5)
        );
        Assertions.assertEquals("Your review on current good already exist", ex.getMessage());
    }


    @Test
    @DisplayName("findAllFullVersionReturnsAll")
    void findAllFullVersionReturnsAll() {
        createReview(user, good, 4);

        Good good2 = createGood("Good2", 3.0, unit);
        User user2= createUser("user2", "userser");

        createReview(user2, good2, 3);

        List<Review> result = reviewHib.findAllFullVersion();

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().allMatch(r -> r.getUser() != null));
        Assertions.assertTrue(result.stream().allMatch(r -> r.getGood() != null));
    }


    @Test
    @DisplayName("getByIdFullVersionIfFound")
    void getByIdFullVersionIfFound() {
        Review saved = createReview(user, good, 4);

        Review result = reviewHib.getByIdFullVersion(saved.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(saved.getId(), result.getId());
        Assertions.assertNotNull(result.getUser());
        Assertions.assertNotNull(result.getGood());
    }

    @Test
    @DisplayName("getByIdFullVersionIfNotFound")
    void getByIdFullVersionIfNotFound() {
        Review result = reviewHib.getByIdFullVersion(99999L);
        Assertions.assertNull(result);
    }


    @Test
    @DisplayName("getByUserAndGoodIfFound")
    void getByUserAndGoodIfFound() {
        createReview(user, good, 4);

        Review result = reviewHib.getByUserAndGood(user.getId(), good.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getUser().getId());
        Assertions.assertEquals(good.getId(), result.getGood().getId());
    }

    @Test
    @DisplayName("getByUserAndGoodIfNotFound")
    void getByUserAndGoodIfNotFound() {
        Review result = reviewHib.getByUserAndGood(99999L, 99999L);
        Assertions.assertNull(result);
    }


    @Test
    @DisplayName("getByUserSmallVersionIfFound")
    void getByUserSmallVersionIfFound() {
        User user2= createUser("newUser", "newnew");
        createReview(user2, good, 3);
        createReview(user, good, 4);

        List<Review> result = reviewHib.getByUserSmallVersion(user.getId(), 0, 10);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(4, result.get(0).getRate());
    }

    @Test
    @DisplayName("getByUserSmallVersionIfEmpty")
    void getByUserSmallVersionIfEmpty() {
        createReview(user, good, 4);
        List<Review> result = reviewHib.getByUserSmallVersion(99999L, 0, 10);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getByUserSmallVersionPagination")
    void getByUserSmallVersionPagination() {
        Good good2 = createGood("Good2", 3.0, unit);
        sessionFactory.getCurrentSession().flush();

        createReview(user, good, 4);
        createReview(user, good2, 3);

        List<Review> result = reviewHib.getByUserSmallVersion(user.getId(), 0, 1);

        Assertions.assertEquals(1, result.size());
    }


    @Test
    @DisplayName("blockReviewIfSuccessful")
    void blockReviewIfSuccessful() {
        Review saved = createReview(user, good, 4);

        boolean result = reviewHib.blockReview(saved.getId(), moderator);

        Assertions.assertTrue(result);
        sessionFactory.getCurrentSession().clear();
        Review updated = reviewHib.findById(saved.getId(), logger);
        Assertions.assertTrue(updated.getBlocked());
    }

    @Test
    @DisplayName("blockReviewIfNotFound")
    void blockReviewIfNotFound() {
        Exception ex = Assertions.assertThrows(DoesNoeExist.class, () ->
                reviewHib.blockReview(99999L, moderator));
        Assertions.assertEquals("Review does not exist with given credentials", ex.getMessage());
    }


    @Test
    @DisplayName("deleteReviewIfSuccessful")
    void deleteReviewIfSuccessful() {
        createReview(user, good, 4);
        reviewHib.deleteReview(good.getId(), user.getId());
        Review found = reviewHib.getByUserAndGood(user.getId(), good.getId());
        Assertions.assertNull(found);
    }

    @Test
    @DisplayName("deleteReviewIfNotFound")
    void deleteReviewIfNotFound() {
        Assertions.assertThrows(
                DoesNoeExist.class,
                () -> reviewHib.deleteReview(99999L, 99999L)
        );
    }


    @Test
    @DisplayName("getFullByFiltersNoFilters")
    void getFullByFiltersNoFilters() {
        createReview(user, good, 4);

        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();

        List<Review> result = reviewHib.getFullByFilters(filters);

        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("getFullByFiltersFilterByGoodId")
    void getFullByFiltersFilterByGoodId() {
        createReview(user, good, 4);
        Good goo1 = createGood("newGOod", 4d, unit);
        createReview(user, goo1, 2);

        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        filters.setGoodId(good.getId());

        List<Review> result = reviewHib.getFullByFilters(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(4, result.get(0).getRate());
    }

    @Test
    @DisplayName("getFullByFiltersFilterByBlocked")
    void getFullByFiltersFilterByBlocked() {
        Review review = createReview(user, good, 4);
        Good goo1 = createGood("newGOod", 2d, unit);

        reviewHib.blockReview(review.getId(), moderator);
        createReview(user,goo1, 1 );

        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        filters.setSortType(ReviewSortTypes.ASC);
        filters.setBlocked(true);

        List<Review> result = reviewHib.getFullByFilters(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(4, result.get(0).getRate());
    }

    @Test
    @DisplayName("getFullByFiltersFilterByRate")
    void getFullByFiltersFilterByRate() {
        createReview(user, good, 4);
        Good goo1 = createGood("newGOod", 2d, unit);
        createReview(user, goo1, 1);

        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        filters.setSortType(ReviewSortTypes.ASC);
        filters.setRate(4);

        List<Review> result = reviewHib.getFullByFilters(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(4, result.get(0).getRate());
    }

    @Test
    @DisplayName("getFullByFiltersWithPagination")
    void getFullByFiltersWithPagination() {
        Good good2 = createGood("Good2", 3.0, unit);
        sessionFactory.getCurrentSession().flush();

        User user2 = createUser("user2", "newUser");

        createReview(user, good, 4);
        createReview(user2, good2, 2);

        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        filters.setSortType(ReviewSortTypes.RATE_ASC);
        filters.setPage(0);
        filters.setSize(1);

        List<Review> result = reviewHib.getFullByFilters(filters);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2, result.get(0).getRate());
    }


    @Test
    @DisplayName("getMinByFiltersWithUser")
    void getMinByFiltersWithUser() {
        createReview(user, good, 4);
        User user2 = createUser("user2", "newUser");
        createReview(user2,good,5);

        ReviewForUserFilters filters = new ReviewForUserFilters();
        filters.setSortType("asc");

        List<Review> result = reviewHib.getMinByFilters(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(4, result.get(0).getRate());
    }

    @Test
    @DisplayName("getMinByFiltersFilterByGoodId")
    void getMinByFiltersFilterByGoodId() {
        createReview(user, good, 4);
        Good good2 = createGood("good2", 3d, unit);
        createReview(user, good2, 3);

        ReviewForUserFilters filters = new ReviewForUserFilters();
        filters.setSortType("desc");
        filters.setGoodId(good.getId());

        List<Review> result = reviewHib.getMinByFilters(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(4, result.get(0).getRate());
    }

    @Test
    @DisplayName("getMinByFiltersFilterByRate")
    void getMinByFiltersFilterByRate() {
        createReview(user, good, 5);
        Good good2 = createGood("good2", 3d, unit);
        createReview(user, good2, 3);

        ReviewForUserFilters filters = new ReviewForUserFilters();
        filters.setSortType("asc");
        filters.setRate(5d);

        List<Review> result = reviewHib.getMinByFilters(filters);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(5, result.get(0).getRate());
    }
}
