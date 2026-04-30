package org.example.core.controllers.system;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.ModeratorRecalcFilter;
import org.example.core.services.documents.ModeratorRecalcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
