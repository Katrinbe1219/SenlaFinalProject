package org.example.core.controllers.users;

import jakarta.validation.Valid;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.users.UserFullDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.exceptions.PermissionDenied;
import org.example.core.hibernate.base_settings.filters.users.UserAdvancedFilter;
import org.example.core.models.types.RoleTypes;
import org.example.core.services.objects.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/moderator/users")
public class UserForModeratorController {

    private UserService userService;
    public UserForModeratorController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/upgrade/{id}")
    // min_user -> max_user
    public StringResponse upgrade(@PathVariable("id") Long userId) {
        if (userId <= 0){
            throw new NotCorrectInput("User Id must be > 0");
        }

        userService.updateRole(userId, RoleTypes.MAX_USER, true);
        return new StringResponse("Upgraded successfully");
    }

    @DeleteMapping("/upgrade/{id}")
    // max-user -> min_user
    public StringResponse deUpgrade(@PathVariable("id") Long userId) {
        if (userId <= 0){
            throw new NotCorrectInput("User Id must be > 0");
        }

        userService.updateRole(userId, RoleTypes.MIN_USER, true);
        return new StringResponse("Upgraded successfully");
    }

    @PatchMapping("/lock/{id}")
    public StringResponse lockUser(
            @PathVariable("id") Long userId
    ){
        if (userId <= 0){
            throw new NotCorrectInput("User Id must be > 0");
        }

        // передается nonLocked значение, заблокирован = !nonLocked
        userService.updateLockedState(userId, false);
        return new StringResponse("Locked successfully");
    }

    @DeleteMapping("/lock/{id}")
    public StringResponse unlockUser(
            @PathVariable("id") Long userId
    ){
        if (userId <= 0){
            throw new NotCorrectInput("User Id must be > 0");
        }
        userService.updateLockedState(userId, true);
        return new StringResponse("Unlocked successfully");
    }

    @GetMapping
    public List<UserFullDto> getAllUsers(
           @Valid @RequestBody UserAdvancedFilter filters
    ){

        if (filters.getRoleType() != null &&
                (filters.getRoleType() != RoleTypes.MAX_USER && filters.getRoleType() != RoleTypes.MIN_USER)){
            throw new PermissionDenied("Your are not allowed to get these users");
        }
        return userService.getAllUsers(filters);
    }

}
