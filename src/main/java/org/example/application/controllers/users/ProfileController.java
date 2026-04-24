package org.example.application.controllers.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.example.application.dto.getting.ProfileDto;
import org.example.application.dto.getting.StringResponse;
import org.example.application.dto.patching.UpdateUserPasswordDto;
import org.example.application.dto.patching.UserDefaultPatchDto;
import org.example.application.exceptions.NonHibernateException;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.exceptions.PermissionDenied;
import org.example.application.hibernate.objects.UserHibImpl;
import org.example.application.mapping.users.ProfileDtoMapper;
import org.example.application.models.User;
import org.example.application.security.DeviceInfoExtractor;
import org.example.application.security.TokenPair;
import org.example.application.services.objects.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@AllArgsConstructor
public class ProfileController {

    private ProfileDtoMapper mapper;
    private UserService service;
    private DeviceInfoExtractor extractor;



    @GetMapping
    public ProfileDto getProfile(){
        try{
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return mapper.toDto(user);
        } catch (Exception e) {
            throw new NonHibernateException("ProfileController getProfile: " + e.getMessage());
        }

    }
    @PatchMapping
    // without password
    public Map<String,String> updateProfile(
            HttpServletRequest request,
            @RequestBody UserDefaultPatchDto dto) throws JsonProcessingException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (Duration.between(user.getUpdatedAt(), Instant.now()).toDays() < 3){
            throw new PermissionDenied("You can not update profile, 3 days did not past from last update");
        }

        if (dto.getNewLogin()!= null && dto.getNewLogin().isEmpty()){
            throw new NotCorrectInput("Login can not be empty");
        }

        if (dto.getNewLogin()!= null && dto.getNewLogin().equals(user.getLogin())){
            throw new NotCorrectInput("Login can not be the same");
        }

        if (dto.getNewUsername() != null && dto.getNewUsername().isEmpty()){
            throw new NotCorrectInput("Username can not be empty");
        }

        if (dto.getNewUsername() != null && dto.getNewUsername().equals(user.getUsernameNotUserDetails())){
            throw new NotCorrectInput("Username can not be the same");
        }

        if (dto.getNewEmail() != null && dto.getNewEmail().equals(user.getEmail())){
            throw new NotCorrectInput("Email can not be the same");
        }

        TokenPair pair= service.patchDefault(dto, user, extractor.extract(request));
        if (pair!= null){
            return Map.of("accessToken", pair.accessToken(), "refreshToken", pair.refreshToken());
        }
        return Map.of("message", "Everything updated successfully");

    }

    @PatchMapping("/password")
    public StringResponse updatePassword(@RequestBody UpdateUserPasswordDto dto){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Duration.between(user.getUpdatedAt(), Instant.now()).toDays() < 3){
            throw new PermissionDenied("You can not update profile, 3 days did not past from last update");
        }

        if (dto.getNewPassword()== null || dto.getNewPassword().isEmpty()){
            throw new NotCorrectInput("New password can not be empty");
        }

        if (dto.getOldPassword()== null || dto.getOldPassword().isEmpty()){
            throw new NotCorrectInput("Old password can not be empty");
        }

        if (dto.getOldPassword()== null || dto.getOldPassword().equals(dto.getNewPassword())){
            throw new NotCorrectInput("Given passwords can not be the same");
        }
        service.patchPassword(dto, user);
        return new StringResponse("Updated successfully");
    }
}
