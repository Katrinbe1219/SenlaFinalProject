package org.example.application.controllers.users;

import org.example.application.dto.getting.StringResponse;
import org.example.application.dto.getting.users.UserForModeratorDto;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.exceptions.PermissionDenied;
import org.example.application.hibernate.base_settings.filters.UserForModeratorFilter;
import org.example.application.models.types.RoleTypes;
import org.example.application.services.objects.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/moderator")
public class UserForModeratorController {

    private UserService userService;
    public UserForModeratorController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/upgrade/{id}")
    // min_user -> max_user
    public StringResponse upgrade(@PathVariable("id") Long userId) {
        if (userId < 0){
            throw new NotCorrectInput("User Id must be more than 0");
        }

        userService.updateRole(userId, RoleTypes.MAX_USER, true);
        return new StringResponse("Upgraded successfully");
    }

    @DeleteMapping("/upgrade/{id}")
    // max-user -> min_user
    public StringResponse deUpgrade(@PathVariable("id") Long userId) {
        if (userId < 0){
            throw new NotCorrectInput("User Id must be more than 0");
        }

        userService.updateRole(userId, RoleTypes.MIN_USER, true);
        return new StringResponse("Upgraded successfully");
    }

    @PatchMapping("/lock/{id}")
    public StringResponse lockUser(
            @PathVariable("id") Long userId
    ){
        if (userId < 0){
            throw new NotCorrectInput("User Id must be more than 0");
        }

        // передается nonLocked значение, заблокирован = !nonLocked
        userService.updateLockedState(userId, false);
        return new StringResponse("Locked successfully");
    }

    @DeleteMapping("/lock/{id}")
    public StringResponse unlockUser(
            @PathVariable("id") Long userId
    ){
        if (userId < 0){
            throw new NotCorrectInput("User Id must be more than 0");
        }
        userService.updateLockedState(userId, true);
        return new StringResponse("Unlocked successfully");
    }

    @GetMapping
    public List<UserForModeratorDto> getAllUsers(
            @RequestBody UserForModeratorFilter filters
    ){

        if (filters.getLocked() != null && filters.getNonLocked()!= null){
            throw new NotCorrectInput("NonLocked and locked can not be given together");
        }
        if (filters.getUpdatedAt() != null
                && (filters.getStartUpdatedDate() != null || filters.getEndUpdatedDate()!= null)){
            throw new NotCorrectInput("Either date or range");
        }

        if (filters.getCreatedAt() != null &&
                (filters.getStartCreatedDate() != null || filters.getEndCreatedDate()!= null)){
            throw new NotCorrectInput("Either date or range");
        }

        if (filters.getRoleType() != null &&
                (filters.getRoleType() != RoleTypes.MAX_USER && filters.getRoleType() != RoleTypes.MIN_USER)){
            throw new PermissionDenied("Your are not allowed to get these users");
        }
        return userService.getAllUsers(filters);
    }

}
