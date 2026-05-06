package org.example.core.controllers;

import jakarta.validation.Valid;
import org.example.core.dto.UnitDto;
import org.example.core.dto.creating.UnitCreateDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.services.dictionaries.UnitService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/units")
public class UnitController {

    UnitService unitService;
    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping
    public List<UnitDto> getAllUnits(
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value="sort", defaultValue = "1", required = false) Integer sort,
            @RequestParam(value="ids",  required = false) List<Long> ids
    ) {
        if (count <=0){
            throw new NotCorrectInput("Count must be greater than 0");
        }

        if (page <0) {
            throw new NotCorrectInput("Page must be >= 0");
        }
        if (ids!=null && ids.isEmpty()) {
            throw new NotCorrectInput("ids length must be > 0");
        }
        BaseSortTypes filters = BaseSortTypes.forValue(sort);
        return unitService.getAll(count, page, filters, ids);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public UnitDto createUnit(@Valid @RequestBody UnitCreateDto unitDto){
        if (unitDto.getFullName() == null || unitDto.getShortName() == null){
            throw new NotCorrectInput("Any name must be given");
        }
        return unitService.create(unitDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public StringResponse deleteUnit(@PathVariable("id") Long id) {
        unitService.deleteById(id);
        return new StringResponse("Deleted unit");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public StringResponse patchUnit(@PathVariable("id") Long id,
                                    @RequestBody Map<String,String> params

    )   {
        if (params.get("shortName") == null && params.get("fullName") == null
                || (params.get("shortName") != null && params.get("shortName").isBlank()  &&
                params.get("fullName") != null && params.get("fullName").isBlank()) ){
            throw new NotCorrectInput("Changes were not given");
        }

        UnitDto dto = new UnitDto();
        dto.setId(id);
        dto.setShortName(params.get("shortName") );
        dto.setFullName(params.get("fullName"));
        unitService.update(dto);

        return new StringResponse("Updated unit");
    }

    @GetMapping("/{id}")
    public UnitDto getUnit(@PathVariable("id") Long id) {
        return unitService.getById(id);
    }
}
