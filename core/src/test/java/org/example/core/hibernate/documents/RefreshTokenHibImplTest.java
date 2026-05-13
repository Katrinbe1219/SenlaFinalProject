package org.example.core.hibernate.documents;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.hibernate.base_settings.filters.RefreshTokenFilter;
import org.example.core.hibernate.base_settings.sorting_types.RefreshTokenSortType;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.models.RefreshToken;
import org.example.core.models.Role;
import org.example.core.models.Unit;
import org.example.core.models.User;
import org.example.core.models.types.RoleTypes;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        RefreshTokenHibImpl.class,
        UserHibImpl.class,
})
@Transactional
public class RefreshTokenHibImplTest {
    private static final Logger logger = LogManager.getLogger(RefreshTokenHibImplTest.class);
    @Autowired
    UserHibImpl userHib;

    @Autowired
    RefreshTokenHibImpl refreshTokenHib;

    @Autowired
    SessionFactory sessionFactory;

    private User basicUser;
    private Role basicRole;

    private User createUser(String login, String username){

        User user = new User();
        user.setLogin(login);
        user.setUsername(username);
        user.setUpdatedAt(Instant.now());
        user.setCreatedAt(Instant.now());
        user.setPassword("pas");
        user.setNonLocked(true);
        user.setRole(basicRole);
        userHib.save(user, logger);
        return user;
    }

    private RefreshToken createToken(User user,
                                     Instant expiresAt,
                                     String hash,
                                     String device){
        RefreshToken token = new RefreshToken();
        token.setCreatedAt(Instant.now());
        token.setUser(user);
        token.setTokenHash(hash);
        if (expiresAt != null){
            token.setExpiresAt(expiresAt);
        }  else{
            token.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
        }

        if (device != null){
            token.setDeviceInfo(device);
        }else{
            token.setDeviceInfo("device");
        }

        token.setLastUsedAt(Instant.now());
        refreshTokenHib.save(token, logger);
        return token;
    }

    @BeforeEach
    void setUp(){
        sessionFactory.getCurrentSession()
                .createNativeQuery("INSERT INTO roles (name) VALUES('MIN_USER') ON CONFLICT DO NOTHING")
                .executeUpdate();

        basicRole =  sessionFactory.getCurrentSession().createQuery("FROM Role WHERE name = :name", Role.class)
                .setParameter("name", RoleTypes.MIN_USER).uniqueResult();

        basicUser = new User();
        basicUser.setLogin("defLogin");
        basicUser.setUsername("defUsername");
        basicUser.setUpdatedAt(Instant.now());
        basicUser.setCreatedAt(Instant.now());
        basicUser.setPassword("pas");
        basicUser.setNonLocked(true);
        basicUser.setRole(basicRole);

        userHib.save(basicUser, logger);
    }


    @Test
    @DisplayName("findAllFullVersionIfNothingFound")
    void findAllFullVersionIfNothingFound(){
        List<RefreshToken> found =  refreshTokenHib.findAllFullVersion();
        Assertions.assertEquals(0, found.size());
    }

    @Test
    @DisplayName("findAllFullVersionIfFound")
    void findAllFullVersionIfFound(){
        User user = createUser("login-new", "new-username");

        createToken(basicUser, null, "hash1", null);
        createToken(user, null, "hash2", null);

        List<RefreshToken> found =  refreshTokenHib.findAllFullVersion();

        Assertions.assertEquals(2, found.size());
        Assertions.assertTrue(found.stream().map(r -> r.getUser().getId()).toList().contains(basicUser.getId()));
        Assertions.assertTrue(found.stream().map(r -> r.getUser().getId()).toList().contains(user.getId()));

    }

    @Test
    @DisplayName("findAllByFiltersWithNoFilters")
    void findAllByFiltersWithNoFilters(){
        User user = createUser("login-new", "new-username");

        createToken(basicUser, null, "hash1", null);
        createToken(user, null, "hash2", null);

        List<RefreshToken> found =  refreshTokenHib.findAllByFilters(new RefreshTokenFilter());

        Assertions.assertEquals(2, found.size());
        Assertions.assertTrue(found.stream().map(r -> r.getUser().getId()).toList().contains(basicUser.getId()));
        Assertions.assertTrue(found.stream().map(r -> r.getUser().getId()).toList().contains(user.getId()));

    }

    @Test
    @DisplayName("findAllByFiltersWithFiltersExpiresAtRange")
    void findAllByFiltersWithFiltersExpiresAtRange(){
        User user = createUser("login-new", "new-username");

        createToken(basicUser, Instant.now().plus(4, ChronoUnit.DAYS), "hash1", null);
        createToken(user, Instant.now().plus(2, ChronoUnit.DAYS), "hash2", null);

        //----------
        RefreshTokenFilter filters = new RefreshTokenFilter();
        filters.setStartExpiresAt(LocalDate.now());
        filters.setEndExpiresAt(LocalDate.now().plus(3, ChronoUnit.DAYS));

        List<RefreshToken> found =  refreshTokenHib.findAllByFilters(filters);

        Assertions.assertEquals(1, found.size());
        Assertions.assertFalse(found.stream().map(r -> r.getUser().getId()).toList().contains(basicUser.getId()));
        Assertions.assertTrue(found.stream().map(r -> r.getUser().getId()).toList().contains(user.getId()));

    }

    @Test
    @DisplayName("findByUserAndDeviceIfNotFound")
    void findByUserAndDeviceIfNotFound(){
        User user = createUser("login-new", "new-username");
        createToken(user, null, "hh", "huawei");
        createToken(basicUser, null, "hash", "device");

        Optional<RefreshToken> found = refreshTokenHib.findByUserAndDevice(basicUser.getId(), "huawei");
        Assertions.assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("findByUserAndDeviceIfFound")
    void findByUserAndDeviceIfFound(){
        User user = createUser("login-new", "new-username");
        createToken(user, null, "hh", "huawei");
        createToken(basicUser, null, "hash", "huawei");

        Optional<RefreshToken> found = refreshTokenHib.findByUserAndDevice(basicUser.getId(), "huawei");
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(found.get().getUser().getId(), basicUser.getId());

    }

    @Test
    @DisplayName("findFullByTokenHashIfNotFound")
    void findFullByTokenHashIfNotFound(){
        createToken(basicUser, null, "hash", null);
        Optional<RefreshToken> found = refreshTokenHib.findFullByTokenHash("hashhh");
        Assertions.assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("findFullByTokenHashIfFound")
    void findFullByTokenHashIfFound(){
        createToken(basicUser, null, "hash", null);
        Optional<RefreshToken> found = refreshTokenHib.findFullByTokenHash("hash");
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(found.get().getUser().getId(), basicUser.getId());
    }

    @Test
    @DisplayName("deleteAllByUserIfTokenForUserExist")
    void deleteAllByUserIfTokenForUserExist(){
        // create tokens for user
        createToken(basicUser, null, "hash1", null);
        createToken(basicUser, Instant.now(), "hash2", "device-to");
        createToken(basicUser, null, "has3", "device2");

        // check existence
        RefreshTokenFilter filters = new RefreshTokenFilter();
        filters.setUserId(basicUser.getId());

        List<RefreshToken> found =  refreshTokenHib.findAllByFilters(filters);
        Assertions.assertEquals(3, found.size());

        refreshTokenHib.deleteAllByUser(basicUser.getId());

        found =  refreshTokenHib.findAllByFilters(filters);
        Assertions.assertEquals(0, found.size());


    }

    @Test
    @DisplayName("deleteAllByUserIfUserNotExist")
    void deleteAllByUserIfTokenForUserNotExist(){
        Exception ex = Assertions.assertThrows(DoesNoeExist.class, ()->
                refreshTokenHib.deleteAllByUser(basicUser.getId()));
        Assertions.assertEquals("Nothing was deleted, check userId", ex.getMessage());
    }

    @Test
    @DisplayName("getActiveSessionsByUserIfSessionsExist")
    void getActiveSessionsByUserIfSessionsExist(){
        createToken(basicUser, Instant.now().plus(1, ChronoUnit.DAYS), "hash1", null);
        createToken(basicUser, Instant.now().minus(1, ChronoUnit.HOURS), "hash2", "device-to");
        createToken(basicUser, Instant.now().plus(1, ChronoUnit.MINUTES), "has3", "device2");

        List<RefreshToken> found = refreshTokenHib.getActiveSessionsByUser(basicUser.getId());
        Assertions.assertEquals(2, found.size());
    }

    @Test
    @DisplayName("getActiveSessionsByUserIfSessionsExist")
    void getActiveSessionsByUserIfSessionsNotExist(){
        createToken(basicUser, Instant.now().minus(1, ChronoUnit.DAYS), "hash1", null);
        createToken(basicUser, Instant.now().minus(1, ChronoUnit.HOURS), "hash2", "device-to");
        createToken(basicUser, Instant.now().minus(1, ChronoUnit.MINUTES), "has3", "device2");

        List<RefreshToken> found = refreshTokenHib.getActiveSessionsByUser(basicUser.getId());
        Assertions.assertEquals(0, found.size());
    }




}
