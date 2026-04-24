package org.example.application.controllers.system;

import lombok.AllArgsConstructor;
import org.example.application.dto.getting.ModeratorRecalcDto;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.hibernate.base_settings.filters.ModeratorRecalcFilter;
import org.example.application.services.documents.ModeratorRecalcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/check/moderator")
@AllArgsConstructor
public class ModeratorRecalculationController {

    private ModeratorRecalcService service;

    @GetMapping
    public List<ModeratorRecalcDto> findAll(
            @RequestBody  ModeratorRecalcFilter filters
    ){
        if (filters.getGoodId() != null && filters.getGoodIds()!= null){
            throw new NotCorrectInput("Either good id or  ids");
        }

        if (filters.getModeratorId() != null && filters.getModeratorIds() != null){
            throw new NotCorrectInput("Either moderator id or ids");
        }

        if (filters.getPage() != null && filters.getPage() < 0){
            throw new NotCorrectInput("page must be >=0");
        }

        if (filters.getCount() != null && filters.getCount() <=0){
            throw new NotCorrectInput("Count must be >0");
        }

        return service.findAllFullVersion(filters);
    }
}
