package org.example.core.hibernate.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.config.IntegrationTestConfig;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.base_settings.filters.users.UserAdvancedFilter;
import org.example.core.hibernate.base_settings.sorting_types.UserForModeratorSortingType;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.models.Role;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IntegrationTestConfig.class,
        UserHibImpl.class
})
@Transactional
class UserHibImplTest {

    @Autowired
    UserHibImpl userHib;

    @Autowired
    SessionFactory sessionFactory;

    private static final Logger logger = LogManager.getLogger(UserHibImplTest.class);

    private Role minUserRole;
    private Role maxUserRole;

    @BeforeEach
    void setUp() {
        Session session = sessionFactory.getCurrentSession();

        session.createNativeQuery("INSERT INTO roles (name) VALUES('MIN_USER') ON CONFLICT DO NOTHING")
                .executeUpdate();
        session.createNativeQuery("INSERT INTO roles (name) VALUES('MAX_USER') ON CONFLICT DO NOTHING")
                .executeUpdate();

        minUserRole = session.createQuery("FROM Role WHERE name = :name", Role.class)
                .setParameter("name", RoleTypes.MIN_USER).uniqueResult();
        maxUserRole = session.createQuery("FROM Role WHERE name = :name", Role.class)
                .setParameter("name", RoleTypes.MAX_USER).uniqueResult();
    }

    private User createUser(String login, String username, String email,
                            Role role, boolean nonLocked) {
        User user = new User();
        user.setLogin(login);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("pass");
        user.setRole(role);
        user.setNonLocked(nonLocked);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userHib.save(user, logger);
        return user;
    }


    @Test
    @Tag("positive")
    @DisplayName("getDefaultRoleReturnsMinUser")
    void getDefaultRoleReturnsMinUser() {
        Role role = userHib.getDefaultRole();

        Assertions.assertNotNull(role);
        Assertions.assertEquals(RoleTypes.MIN_USER, role.getName());
    }


    @Test
    @Tag("positive")
    @DisplayName("findByUsernameOrLoginOrEmailByAll")
    void findByUsernameOrLoginOrEmailByAll() {
        createUser("qwe", "uniqueuser", "unique@mail.com", minUserRole, true);
        createUser("qwerty", "other", "unique55@mail.com", minUserRole, true);
        createUser("qwertytru", "otherqw", "other@mail.com", minUserRole, true);

        List<User> result = userHib.findByUsernameOrLoginOrEmail(
                "qwe", "other", "other@mail.com"
        );

        Assertions.assertFalse(result.isEmpty());
       Assertions.assertEquals(3, result.size());
    }

    @Test
    @Tag("positive")
    @DisplayName("findByUsernameOrLoginOrEmailByEmail")
    void findByUsernameOrLoginOrEmailByEmail() {
        createUser("l", "uA", "emailA@mail.com", minUserRole, true);
        createUser("la", "uB", "emailasdA@mail.com", minUserRole, true);

        List<User> result = userHib.findByUsernameOrLoginOrEmail(
                "other", "other", "emailA@mail.com"
        );

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
    }



    @Test
    @Tag("negative")
    @DisplayName("findByUsernameOrLoginOrEmailIfNotFound")
    void findByUsernameOrLoginOrEmailIfNotFound() {
        List<User> result = userHib.findByUsernameOrLoginOrEmail(
                "nonexistent", "nonexistent", "nonexistent@mail.com"
        );

        Assertions.assertTrue(result.isEmpty());
    }


    @Test
    @Tag("positive")
    @DisplayName("getUserByLoginFullVersionIfFound")
    void getUserByLoginFullVersionIfFound() {
        User u1 = createUser("fulllogin", "fulluser", "full@mail.com", minUserRole, true);

        User result = userHib.getUserByLoginFullVersion("fulllogin");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(u1.getLogin(), result.getLogin());
        Assertions.assertNotNull(result.getRole());
    }

    @Test
    @Tag("negative")
    @DisplayName("getUserByLoginFullVersionIfNotFound")
    void getUserByLoginFullVersionIfNotFound() {
        Assertions.assertThrows(
                NonHibernateException.class,
                () -> userHib.getUserByLoginFullVersion("nonexistent")
        );
    }


    @Test
    @Tag("positive")
    @DisplayName("getByUsernameSmallVersionIfFound")
    void getByUsernameSmallVersionIfFound() {
        User u= createUser("l", "username", "e@mail.com", minUserRole, true);

        User result = userHib.getByUsernameSmallVersion("username");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(u.getUsernameNotUserDetails(), result.getUsernameNotUserDetails());
    }

    @Test
    @Tag("negative")
    @DisplayName("getByUsernameSmallVersionIfNotFound")
    void getByUsernameSmallVersionIfNotFound() {
        User result = userHib.getByUsernameSmallVersion("nonexistent");
        Assertions.assertNull(result);
    }


    @Test
    @Tag("positive")
    @DisplayName("getByLoginSmallVersionIfFound")
    void getByLoginSmallVersionIfFound() {
        User u = createUser("login", "u", "e@mail.com", minUserRole, true);

        User result = userHib.getByLoginSmallVersion("login");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(u.getLogin(), result.getLogin());
    }

    @Test
    @Tag("negative")
    @DisplayName("getByLoginSmallVersionIfNotFound")
    void getByLoginSmallVersionIfNotFound() {
        User result = userHib.getByLoginSmallVersion("smth");
        Assertions.assertNull(result);
    }


    @Test
    @Tag("positive")
    @DisplayName("getUserIdByLoginIfFound")
    void getUserIdByLoginIfFound() {
        User user = createUser("login", "username", "i@mail.com", minUserRole, true);

        Long result = userHib.getUserIdByLogin("login");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result);
    }

    @Test
    @Tag("negative")
    @DisplayName("getUserIdByLoginIfNotFound")
    void getUserIdByLoginIfNotFound() {
        Long result = userHib.getUserIdByLogin("smth");
        Assertions.assertNull(result);
    }


    @Test
    @Tag("positive")
    @DisplayName("getRoleByNameIfFound")
    void getRoleByNameIfFound() {
        Role role = userHib.getRoleByName(RoleTypes.MIN_USER);

        Assertions.assertNotNull(role);
        Assertions.assertEquals(RoleTypes.MIN_USER, role.getName());
    }

    @Test
    @Tag("negative")
    @DisplayName("getRoleByNameIfNotFound")
    void getRoleByNameIfNotFound() {
        Role role = userHib.getRoleByName(RoleTypes.ANALYST); // не создана в тестовой бд
        Assertions.assertNull(role);
    }


    @Test
    @Tag("positive")
    @DisplayName("gtByIdFullVersionIfFound")
    void gtByIdFullVersionIfFound() {
        User user = createUser("fullvlogin", "fullvuser", "fullv@mail.com", minUserRole, true);

        User result = userHib.gtByIdFullVersion(user.getId());

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getRole());
    }

    @Test
    @Tag("negative")
    @DisplayName("gtByIdFullVersionIfNotFound")
    void gtByIdFullVersionIfNotFound() {
        User result = userHib.gtByIdFullVersion(99999L);
        Assertions.assertNull(result);
    }


    @Test
    @Tag("positive")
    @DisplayName("getUsersByFilterReturnsOnlyMinAndMaxByDefault")
    void getUsersByFilterReturnsOnlyMinAndMaxByDefault() {
        createUser("l", "u", "h@mail.com", minUserRole, true);
        createUser("l1", "u1", "u@mail.com", maxUserRole, true);

        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setSortType(UserForModeratorSortingType.ASC);

        List<User> result = userHib.getUsersByFilter(filters, true);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(result.stream().allMatch(u ->
                u.getRole().getName() == RoleTypes.MIN_USER ||
                        u.getRole().getName() == RoleTypes.MAX_USER
        ));
    }

    @Test
    @Tag("positive")
    @DisplayName("getUsersByFilterByRole")
    void getUsersByFilterByRole() {
        createUser("l", "u", "a@mail.com", minUserRole, true);
        createUser("l1", "u1", "u@mail.com", maxUserRole, true);
        createUser("l23", "u2", "a123@mail.com", minUserRole, true);

        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setSortType(UserForModeratorSortingType.ASC);
        filters.setRoleType(RoleTypes.MIN_USER);

        List<User> result = userHib.getUsersByFilter(filters, true);
        Assertions.assertEquals(2, result.size());

        }

    @Test
    @Tag("positive")
    @DisplayName("getUsersByFilterLockedOnly")
    void getUsersByFilterLockedOnly() {
        createUser("l", "i", "l@mail.com", minUserRole, false);
        createUser("l1", "u1", "a@mail.com", minUserRole, true);

        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setSortType(UserForModeratorSortingType.ASC);
        filters.setLocked(true);

        List<User> result = userHib.getUsersByFilter(filters, true);

        Assertions.assertTrue(result.stream().allMatch(u -> !u.getNonLocked()));
    }

    @Test
    @Tag("positive")
    @DisplayName("getUsersByFilterEmptyResult")
    void getUsersByFilterEmptyResult() {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setSortType(UserForModeratorSortingType.ASC);
        filters.setRoleType(RoleTypes.ANALYST); // нет  в бд тестовой

        List<User> result = userHib.getUsersByFilter(filters, true);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Tag("positive")
    @DisplayName("getUsersByFilterOrderPreserved")
    void getUsersByFilterOrderPreserved() {
        createUser("д", "u1", "z@mail.com", minUserRole, true);
        createUser("l1", "u2", "a@mail.com", minUserRole, true);

        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setSortType(UserForModeratorSortingType.LOGIN_ASC);

        List<User> result = userHib.getUsersByFilter(filters, true);

        List<String> logins = result.stream().map(User::getLogin).toList();
        for (int i = 0; i < logins.size() - 1; i++) {
            Assertions.assertTrue(logins.get(i).compareTo(logins.get(i + 1)) <= 0);
        }
    }
}
