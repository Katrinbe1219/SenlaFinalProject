package org.example.core.services.objects;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.auth.RegisterDto;
import org.example.core.dto.getting.users.UserFullDto;
import org.example.core.dto.patching.UpdateUserPasswordDto;
import org.example.core.dto.patching.UserDefaultPatchDto;
import org.example.core.exceptions.*;
import org.example.core.hibernate.documents.RefreshTokenHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.mapping.users.UserFullMapper;
import org.example.core.models.Role;
import org.example.core.models.User;
import org.example.core.models.types.RoleTypes;
import org.example.core.security.JwtService;
import org.example.core.security.TokenPair;
import org.example.core.services.auth.RefreshTokenService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserHibImpl userHib;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtService jwtService;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    RefreshTokenHibImpl refreshTokenHib;

    @Mock
    UserFullMapper mapper;

    @InjectMocks
    UserService service;

    @Test
    @Tag("negative")
    @DisplayName("patchDefaultIfUsernameExists")
    void patchDefaultIfUsernameExists(){
        UserDefaultPatchDto dto  = new UserDefaultPatchDto();
        dto.setNewUsername("username");
        User user = new User();
        user.setUsername("username");

        when(userHib.getByUsernameSmallVersion(anyString()))
                .thenReturn(user);

        Exception ex = Assertions.assertThrows(EntityAlreadyExist.class, ()->
                service.patchDefault(dto, null, null));

        Assertions.assertEquals("User with given username already exists", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("patchDefaultIfLoginExists")
    void patchDefaultIfLoginExists(){
        UserDefaultPatchDto dto  = new UserDefaultPatchDto();
        dto.setNewLogin("username");
        User user = new User();
        user.setLogin("username");

        when(userHib.getByLoginSmallVersion(anyString()))
                .thenReturn(user);

        Exception ex = Assertions.assertThrows(EntityAlreadyExist.class, ()->
                service.patchDefault(dto, null, null));

        Assertions.assertEquals("User with given login already exists", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("patchDefaultIfEmailExists")
    void patchDefaultIfEmailExists(){
        UserDefaultPatchDto dto  = new UserDefaultPatchDto();
        dto.setNewEmail("username");
        User user = new User();
        user.setEmail("username");

        when(userHib.getByEmailSmallVersion(anyString()))
                .thenReturn(user);

        Exception ex = Assertions.assertThrows(EntityAlreadyExist.class, ()->
                service.patchDefault(dto, null, null));

        Assertions.assertEquals("User with given email already exists", ex.getMessage());
    }

    @Test
    @Tag("positive")
    @DisplayName("patchDefaultIfLoginWasGiven")
    void patchDefaultIfLoginWasGiven(){
        UserDefaultPatchDto dto  = new UserDefaultPatchDto();
        dto.setNewLogin("username");
        User user = new User();


        when(userHib.getByLoginSmallVersion(anyString()))
                .thenReturn(null);
        when(jwtService.generateToken(any(User.class)))
                .thenReturn("access-token");
        when(refreshTokenService.createToken(any(User.class), anyString()))
                .thenReturn("refresh-token");
        Assertions.assertEquals(new TokenPair("access-token", "refresh-token"),
                service.patchDefault(dto, user, "device"));


    }

    @Test
    @Tag("positive")
    @DisplayName("patchDefaultIfLoginWasNotGiven")
    void patchDefaultIfLoginWasNotGiven(){
        UserDefaultPatchDto dto  = new UserDefaultPatchDto();
        dto.setNewUsername("username");
        User user = new User();

        when(userHib.getByUsernameSmallVersion(anyString()))
                .thenReturn(null);

        Assertions.assertNull(service.patchDefault(dto, user, "device"));

    }

    @Test
    @Tag("positive")
    @DisplayName("patchPasswordIfSuccessful")
    void patchPasswordIfSuccessful(){
        UpdateUserPasswordDto dto = new UpdateUserPasswordDto();
        dto.setNewPassword("password-new");
        dto.setOldPassword("password");

        User user = new User();
        user.setPassword("password");
        user.setId(1L);

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(Boolean.FALSE);
        when(passwordEncoder.encode(anyString())).thenReturn("password-new");

        service.patchPassword(dto, user);
        verify(refreshTokenHib).deleteAllByUser(anyLong());

    }

    @Test
    @Tag("negative")
    @DisplayName("patchPasswordIfPasswordsAreTheSame")
    void patchPasswordIfPasswordsAreTheSame(){
        UpdateUserPasswordDto dto = new UpdateUserPasswordDto();
        dto.setNewPassword("password");
        dto.setOldPassword("password");

        User user = new User();
        user.setPassword("password");

        when(passwordEncoder.encode(anyString())).thenReturn("password");

        Exception ex = Assertions.assertThrows(NotCorrectInput.class,()->service.patchPassword(dto, user));
        Assertions.assertEquals("Password can not be the same", ex.getMessage());
        verify(refreshTokenHib, never()).deleteAllByUser(anyLong());

    }

    @Test
    @Tag("negative")
    @DisplayName("patchPasswordIfOldPasswordIsInvalid")
    void patchPasswordIfOldPasswordIsInvalid(){
        UpdateUserPasswordDto dto = new UpdateUserPasswordDto();
        dto.setNewPassword("password-new");
        dto.setOldPassword("password-old");

        User user = new User();
        user.setPassword("password-old");

        when(passwordEncoder.matches(any(), anyString()))
                .thenReturn(Boolean.TRUE);

        Exception ex = Assertions.assertThrows(NotCorrectInput.class,()->service.patchPassword(dto, user));
        Assertions.assertEquals("Old password is not valid", ex.getMessage());
        verify(refreshTokenHib, never()).deleteAllByUser(anyLong());

    }

    @Test
    @Tag("negative")
    @DisplayName("patchPasswordIfOldPasswordIsInvalid")
    void updateRoleIfUserNotFound(){
        when(userHib.gtByIdFullVersion(anyLong()))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(DoesNoeExist.class, () ->
                service.updateRole(1L, RoleTypes.ADMIN, Boolean.TRUE)
                );

        Assertions.assertEquals("user does not exist with given credentials", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("updateRoleIfRoleIsTheSame")
    void updateRoleIfRoleIsTheSame(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleTypes.ADMIN);
        user.setRole(role);

        when(userHib.gtByIdFullVersion(anyLong()))
                .thenReturn(user);
        Exception ex = Assertions.assertThrows(NotCorrectInput.class, () ->
                service.updateRole(1L, RoleTypes.ADMIN, Boolean.TRUE)
        );

        Assertions.assertEquals("User already had this role", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("updateRoleIfPermissionDenied")
    void updateRoleIfPermissionDenied(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleTypes.ADMIN);
        user.setRole(role);

        when(userHib.gtByIdFullVersion(anyLong()))
                .thenReturn(user);
        Exception ex = Assertions.assertThrows(PermissionDenied.class, () ->
                service.updateRole(1L, RoleTypes.MIN_USER, Boolean.TRUE)
        );

        Assertions.assertEquals("You have no authorities to change role for current user", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("updateRoleIfRoleNotFound")
    void updateRoleIfRoleNotFound(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleTypes.ADMIN);
        user.setRole(role);

        when(userHib.gtByIdFullVersion(anyLong()))
                .thenReturn(user);
        when(userHib.getRoleByName(any())).thenReturn(null);
        Exception ex = Assertions.assertThrows(NonHibernateException.class, () ->
                service.updateRole(1L, RoleTypes.MIN_USER, Boolean.FALSE)
        );

        Assertions.assertTrue(ex.getMessage().contains("Unexpected - Role does not exist "));
    }

    @Test
    @Tag("positive")
    @DisplayName("updateRoleIfSuccessful")
    void updateRoleIfSuccessful(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleTypes.ADMIN);
        user.setRole(role);

        when(userHib.gtByIdFullVersion(anyLong()))
                .thenReturn(user);
        when(userHib.getRoleByName(any())).thenReturn(new Role());

        service.updateRole(1L, RoleTypes.MIN_USER, Boolean.FALSE);
        verify(userHib).update(any(), any(Logger.class));

    }

    @Test
    @Tag("negative")
    @DisplayName("updateLockedStateIfUserNotFound")
    void updateLockedStateIfUserNotFound(){
        when(userHib.gtByIdFullVersion(anyLong())).thenReturn(null);
        Exception ex = Assertions.assertThrows(DoesNoeExist.class, () ->
                service.updateLockedState(1L, false));
        Assertions.assertEquals("user does not exist with given credentials", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("updateLockedStateIfUserNotFound")
    void updateLockedStateIfUserAlreadyHaveState(){
        User user = new User();
        user.setNonLocked(false);

        when(userHib.gtByIdFullVersion(anyLong())).thenReturn(user);

        Exception ex = Assertions.assertThrows(NotCorrectInput.class, () ->
                service.updateLockedState(1L, false));
        Assertions.assertEquals("User already had this locked state", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("updateLockedStateIfPermissionDenied")
    void updateLockedStateIfPermissionDenied(){
        User user = new User();
        user.setNonLocked(true);
        Role role= new Role();
        role.setName(RoleTypes.ADMIN);
        user.setRole(role);

        when(userHib.gtByIdFullVersion(anyLong())).thenReturn(user);

        Exception ex = Assertions.assertThrows(PermissionDenied.class, () ->
                service.updateLockedState(1L, false));
        Assertions.assertEquals("You have no authorities to change role for current user", ex.getMessage());
    }

    @Test
    @Tag("positive")
    @DisplayName("updateLockedStateIfSuccessful")
    void updateLockedStateIfSuccessful(){
        User user = new User();
        user.setNonLocked(true);
        Role role= new Role();
        role.setName(RoleTypes.MIN_USER);
        user.setRole(role);

        when(userHib.gtByIdFullVersion(anyLong())).thenReturn(user);
        service.updateLockedState(1L, false);
        verify(userHib).update(any(), any(Logger.class));
    }

    @Test
    @Tag("negative")
    @DisplayName("createUserIfRoleNotExist")
    void createUserIfRoleNotExist(){

        when(userHib.getRoleByName(any(RoleTypes.class))).thenReturn(null);
        Exception ex = Assertions.assertThrows(DoesNoeExist.class, () ->
                service.createUser(RoleTypes.ADMIN, null));
        Assertions.assertTrue(ex.getMessage().contains("Unexpected - Role does not exist "));
    }

    @Test
    @Tag("negative")
    @DisplayName("createUserIfConflicts")
    void createUserIfConflicts(){
        RegisterDto dto = new RegisterDto();
        dto.setLogin("Login");
        dto.setPassword("Password");
        dto.setEmail("email");
        dto.setUsername("sad");

        User con = new User();
        con.setLogin("Login");
        con.setEmail("email");
        con.setUsername("sadlyyy");


        when(userHib.getRoleByName(any(RoleTypes.class))).thenReturn(new Role());
        when(userHib.findByUsernameOrLoginOrEmail(any(), any(), any()))
                .thenReturn(List.of(con));
        RegistrationException ex = Assertions.assertThrows(RegistrationException.class, () ->
                service.createUser(RoleTypes.ADMIN, dto));
        Assertions.assertTrue(ex.getErrors().containsKey("login"));
        Assertions.assertTrue(ex.getErrors().containsKey("email"));
        Assertions.assertFalse(ex.getErrors().containsKey("username"));
    }


    @Test
    @Tag("positive")
    @DisplayName("createUserIfSuccessful")
    void createUserIfSuccessful(){
        RegisterDto dto = new RegisterDto();
        dto.setLogin("Login");
        dto.setPassword("Password");
        dto.setEmail("email");
        dto.setUsername("sad");


        when(userHib.getRoleByName(any(RoleTypes.class))).thenReturn(new Role());
        when(userHib.findByUsernameOrLoginOrEmail(any(), any(), any())).thenReturn(List.of());
        UserFullDto user = new UserFullDto();
        when(mapper.toDto(any())).thenReturn(user);
        Assertions.assertEquals(user, service.createUser(RoleTypes.ADMIN, dto));

    }

}
