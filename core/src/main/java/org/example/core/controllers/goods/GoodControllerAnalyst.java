package org.example.core.controllers.goods;

import jakarta.validation.Valid;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.services.objects.GoodService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/goods/analyst")
public class GoodControllerAnalyst {

    private GoodService goodService;
    public GoodControllerAnalyst(GoodService goodService) {
        this.goodService = goodService;
    }
    @GetMapping
    @PreAuthorize("hasRole('ANALYST')")
    public List<GoodGetFullDto> findAllAnalyst(
            @Valid @RequestBody GoodFilter filters
    ){


        if (filters.getSize() <=0 ){
            throw new NotCorrectInput("Size must be greater than 0 or null");
        }

        if (filters.getPage() < 0){
            throw new NotCorrectInput("Page must be more or equals 0 or null");
        }


        return goodService.findAllForAnalyst(filters);
    }
}
