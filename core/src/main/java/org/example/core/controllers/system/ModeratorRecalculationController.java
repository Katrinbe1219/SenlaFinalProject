package org.example.core.controllers.system;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.creating.ManyModeratorLogCreateDto;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.ModeratorRecalcFilter;
import org.example.core.models.User;
import org.example.core.services.documents.ModeratorRecalcService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("moderator/check")
@AllArgsConstructor
public class ModeratorRecalculationController {

    private ModeratorRecalcService service;

    @GetMapping
    public List<ModeratorRecalcDto> findAll(
           @Valid @RequestBody  ModeratorRecalcFilter filters
    ){
        return service.findAllFullVersion(filters);
    }

    @PatchMapping("/change-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public StringResponse changeStatusForMany(
            @Valid @RequestBody ManyModeratorLogCreateDto dto,
            @AuthenticationPrincipal User user
    ){

        service.addManyLogs(dto, user);
        return new StringResponse("Goods' statuses were changed");
    }

}
