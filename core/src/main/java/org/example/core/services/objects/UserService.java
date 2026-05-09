package org.example.core.services.objects;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.auth.RegisterDto;
import org.example.core.dto.getting.users.UserFullDto;
import org.example.core.dto.patching.UpdateUserPasswordDto;
import org.example.core.dto.patching.UserDefaultPatchDto;
import org.example.core.exceptions.*;
import org.example.core.hibernate.base_settings.filters.users.UserAdvancedFilter;
import org.example.core.hibernate.documents.RefreshTokenHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.mapping.users.UserFullMapper;
import org.example.core.models.Role;
import org.example.core.models.User;
import org.example.core.models.types.RoleTypes;
import org.example.core.security.JwtService;
import org.example.core.security.TokenPair;
import org.example.core.services.auth.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    private UserHibImpl userHib;
    private PasswordEncoder passwordEncoder;
    private RefreshTokenService refreshTokenService;
    private JwtService jwtService;
    private RefreshTokenHibImpl refreshHib;

    private UserFullMapper mapper;



    @Transactional
    public TokenPair patchDefault(UserDefaultPatchDto dto, User user, String deviceInfo){
        try{
            User conflict = null;
            if (dto.getNewUsername()!=null){
                conflict = userHib.getByUsernameSmallVersion(dto.getNewUsername());
                if (conflict!= null){
                    throw new EntityAlreadyExist("User with given username already exists");
                }
                user.setUsername(dto.getNewUsername());
            }

            if (dto.getNewLogin() != null){
                conflict = userHib.getByLoginSmallVersion(dto.getNewLogin());
                if (conflict!= null){
                    throw new EntityAlreadyExist("User with given login already exists");
                }
                user.setLogin(dto.getNewLogin());
            }

            if (dto.getNewEmail() != null){
                conflict= userHib.getByEmailSmallVersion(dto.getNewEmail());
                if (conflict!=null){
                    throw new EntityAlreadyExist("User with given email already exists");
                }

                user.setEmail(dto.getNewEmail());
            }
            user.setUpdatedAt(Instant.now());
            userHib.update(user, logger);

            if (dto.getNewLogin() != null){
                String newToken = jwtService.generateToken(user);
                String refreshToken = refreshTokenService.createToken(user, deviceInfo);

                return new TokenPair(newToken, refreshToken);
            }
            return null;
        } catch (Exception e) {
            logger.error("UserService patchDefault: " + e.getMessage());
            throw e;
        }


    }

    @Transactional
    public void patchPassword(UpdateUserPasswordDto dto, User user){
        try{
            String oldPassword = passwordEncoder.encode(dto.getOldPassword());
            String newPassword = passwordEncoder.encode(dto.getNewPassword());

            if (user.getPassword().equals(newPassword)){
                throw new NotCorrectInput("Password can not be the same");
            }

            if (!passwordEncoder.matches(oldPassword, user.getPassword())){
                throw new NotCorrectInput("Old password is not valid");
            }


            user.setPassword(newPassword);
            user.setUpdatedAt(Instant.now());
            userHib.update(user, logger);
            refreshHib.deleteAllByUser(user.getId());
        }catch (Exception e){
            logger.error("UserService patchPassword: " + e.getMessage());
            throw e;
        }



    }

    @Transactional
    public void updateRole(Long userId, RoleTypes type, boolean isNotAdmin){
        try{
            User user = userHib.gtByIdFullVersion(userId);
            if (user == null){
                throw new DoesNoeExist("user does not exist with given credentials");
            }
            if (user.getRole().getName() == type){
                throw new NotCorrectInput("User already had this role");
            }

            if (user.getRole().getName() != RoleTypes.MIN_USER
                    && user.getRole().getName() != RoleTypes.MAX_USER && isNotAdmin){

                throw new PermissionDenied("You have no authorities to change role for current user");
            }

            Role role = userHib.getRoleByName(type);
            if (role == null){
                throw new NonHibernateException("Unexpected - Role does not exist " + type.name());
            }
            user.setRole(role);
            userHib.update(user, logger);
        }catch (Exception e){
            logger.error("UserService updateRole: " + e.getMessage());
            throw e;
        }



    }

    @Transactional
    public void updateLockedState(Long userId, boolean nonLocked){
        try{
            User user = userHib.gtByIdFullVersion(userId);
            if (user == null){
                throw new DoesNoeExist("user does not exist with given credentials");
            }
            if (user.getNonLocked() == nonLocked){
                throw new NotCorrectInput("User already had this locked state");
            }
            if (user.getRole().getName() != RoleTypes.MIN_USER
                    && user.getRole().getName() != RoleTypes.MAX_USER){

                throw new PermissionDenied("You have no authorities to change role for current user");
            }

            user.setNonLocked(nonLocked);
            userHib.update(user, logger);
        }catch (Exception e){
            logger.error("UserService updateLockedState: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<UserFullDto> getAllUsers(UserAdvancedFilter filters){
        try{
            List<User> users = userHib.getUsersByFilter(filters);
            if (users == null){
                return List.of();
            }
            return toList(users);
        }catch (Exception e){
            logger.error("UserService getAllUsers: " + e.getMessage());
            throw e;
        }


    }

    @Transactional
    public UserFullDto createUser(RoleTypes type, RegisterDto dto){
        try{
            Role role = userHib.getRoleByName(type);
            if (role == null){
                throw new DoesNoeExist("Unexpected - Role does not exist " + type.name());
            }

            List<User> conflicts = userHib.findByUsernameOrLoginOrEmail(dto.getLogin(), dto.getUsername(), dto.getEmail());
            if (!conflicts.isEmpty()){
                Map<String, String> errors = new HashMap<>();
                boolean loginConflict = conflicts.stream().anyMatch(s -> dto.getLogin().equals(s.getLogin()));
                boolean emailConflict = conflicts.stream().anyMatch(s -> s.getEmail() != null &&  dto.getEmail().equals(s.getEmail()));
                boolean usernameConflict = conflicts.stream().anyMatch(s -> dto.getUsername().equals(s.getUsernameNotUserDetails()));

                if (loginConflict) errors.put("login", "already exists");
                if (emailConflict) errors.put("email", "already exists");
                if (usernameConflict) errors.put("username","already exists");
                throw new RegistrationException(errors);
            }
            User user = new User();
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            user.setRole(role);
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setLogin(dto.getLogin());
            user.setNonLocked(true);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            User newUser = userHib.save(user, logger);
            return mapper.toDto(newUser);
        }
        catch(RegistrationException| DoesNoeExist e){
            throw e;
        }
        catch(Exception e){
            logger.error("UserService createUser: " + e.getMessage());
            throw new NonHibernateException("UserService createUser: " + e.getMessage());
        }
    }

    @Transactional
    public UserFullDto getUserById(Long id){
        try{
            User user = userHib.gtByIdFullVersion(id);
            return mapper.toDto(user);
        }catch (Exception e){
            logger.error("UserService getUserById: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public void deleteUser(Long id){
        try{
            userHib.delete(id, logger);
        }catch (Exception e){
            logger.error("UserService deleteUser: " + e.getMessage());
            throw e;
        }

    }

    private List<UserFullDto> toList(List<User> users){
        List<UserFullDto> dtos = new ArrayList<>();
        for (User user : users){
            dtos.add(mapper.toDto(user));
        }
        return dtos;
    }

}
