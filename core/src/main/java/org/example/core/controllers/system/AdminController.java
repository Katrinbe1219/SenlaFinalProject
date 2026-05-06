package org.example.core.controllers.system;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.auth.RegisterDto;
import org.example.core.dto.getting.RefreshTokenDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.users.UserFullDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.RefreshTokenFilter;
import org.example.core.hibernate.base_settings.filters.users.UserAdvancedFilter;
import org.example.core.models.types.RoleTypes;
import org.example.core.services.auth.RefreshTokenService;
import org.example.core.services.objects.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    // просмотр сессий по userId
    // сброс сессий по userId -> изменил роль, а токен то еще живет со старой информацией
    private UserService userService;
    private RefreshTokenService refreshTokenService;


    @GetMapping("/users")
    public List<UserFullDto> getUsers(@Valid @RequestBody UserAdvancedFilter filters){
        return userService.getAllUsers(filters);
    }

    @PostMapping("/users/analyst")
    public UserFullDto postAnalyst(
            @Valid @RequestBody RegisterDto  dto
    ){
        return userService.createUser(RoleTypes.ANALYST, dto);
    }

    @PostMapping("/users/user")
    public UserFullDto postUser(
            @RequestParam(value = "type", defaultValue = "min_user", required = false) String type,
            @Valid @RequestBody RegisterDto  dto
    ){
        if (type.equalsIgnoreCase("min_user")){
            return userService.createUser(RoleTypes.MIN_USER, dto);
        }else if (type.equalsIgnoreCase("max_user")){
            return userService.createUser(RoleTypes.MAX_USER, dto);
        }else{
            throw new NotCorrectInput("Either min_user or max_user role");
        }
    }

    @PostMapping("/users/moderator")
    public UserFullDto postModerator(
            @Valid @RequestBody RegisterDto  dto
    ){
        return userService.createUser(RoleTypes.MODERATOR, dto);
    }

    @GetMapping("/users/{id}")
    public UserFullDto getUserById(@PathVariable("id") Long id){
        if (id <=0){
            throw new NotCorrectInput("Id must be > 0");
        }
        return userService.getUserById(id);
    }

    @DeleteMapping("/users/{id}")
    public StringResponse deleteUserById(@PathVariable("id") Long id){
        if (id <=0){
            throw new NotCorrectInput("Id must be > 0");
        }
        userService.deleteUser(id);
        return  new StringResponse("User is successfully deleted");
    }

    @PatchMapping("/users/{id}/role")
    public StringResponse patchRole(@PathVariable("id") Long id,
                          @RequestParam("role") String role){
        if (id <=0){
            throw new NotCorrectInput("Id must be > 0");
        }
        RoleTypes roleType = RoleTypes.getFromString(role);
        userService.updateRole(id, roleType, false);
        return new StringResponse("Role has been updated successfully");
    }

    @GetMapping("/sessions")
    public List<RefreshTokenDto> getSessions(@Valid @RequestBody  RefreshTokenFilter filters){
        return refreshTokenService.getAllByFilters(filters);
    }

    @GetMapping("/sessions/{id}")
    public List<RefreshTokenDto> getSessionsByUserId(@PathVariable("id") Long userId){
        if (userId <=0){
            throw new NotCorrectInput("Id must be > 0");
        }
        return refreshTokenService.getTokensByUserId(userId);
    }

    @DeleteMapping("/sessions/{userid}")
    public StringResponse deleteSessionsByUserId(@PathVariable("userid") Long userId){
        if (userId <=0){
            throw new NotCorrectInput("Id must be > 0");
        }
        refreshTokenService.revokeByUserId(userId);
        return new StringResponse("Sessions has been deleted successfully");
    }


}
