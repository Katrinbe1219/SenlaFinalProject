package org.example.core.controllers.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.getting.ProfileDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.patching.UpdateUserPasswordDto;
import org.example.core.dto.patching.UserDefaultPatchDto;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.exceptions.PermissionDenied;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.mapping.users.ProfileDtoMapper;
import org.example.core.models.User;
import org.example.core.security.DeviceInfoExtractor;
import org.example.core.security.TokenPair;
import org.example.core.services.objects.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ProfileDto getProfile(
            @AuthenticationPrincipal User user
    ){
        try{
            return mapper.toDto(user);
        } catch (Exception e) {
            throw new NonHibernateException("ProfileController getProfile: " + e.getMessage());
        }

    }
    @PatchMapping
    // without password
    public Map<String,String> updateProfile(
            HttpServletRequest request,
           @Valid @RequestBody UserDefaultPatchDto dto,
            @AuthenticationPrincipal User user)  {


        if (Duration.between(user.getUpdatedAt(), Instant.now()).toDays() < 3){
            throw new PermissionDenied("You can not update profile, 3 days did not past from last update");
        }


        if (dto.getNewLogin()!= null && dto.getNewLogin().equals(user.getLogin())){
            throw new NotCorrectInput("Login can not be the same");
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
    public StringResponse updatePassword(@Valid @RequestBody UpdateUserPasswordDto dto,
                                         @AuthenticationPrincipal User  user){

        if (Duration.between(user.getUpdatedAt(), Instant.now()).toDays() < 3){
            throw new PermissionDenied("You can not update profile, 3 days did not past from last update");
        }


        if ( dto.getOldPassword().equals(dto.getNewPassword())){
            throw new NotCorrectInput("Given passwords can not be the same");
        }
        service.patchPassword(dto, user);
        return new StringResponse("Updated successfully");
    }
}
