package org.example.core.controllers;

import jakarta.validation.Valid;
import org.example.core.dto.DistrictDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.services.dictionaries.DistrictService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/districts")
public class DistrictController {
    DistrictService districtService;

    public DistrictController(DistrictService districtService) {
        this.districtService = districtService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<DistrictDto> getAll(
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value="sort", defaultValue = "1", required = false) Integer sort,
            @RequestParam(value="ids",  required = false) List<Long> ids
    ){
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
        return districtService.getAll(count, page, filters, ids);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DistrictDto getById(@PathVariable("id") Long id){
        return districtService.getById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public StringResponse deleteById(@PathVariable("id") Long id){
        if (id <= 0){
            throw new NotCorrectInput("id must be > 0");
        }
        districtService.deleteDistrict(id);
        return new StringResponse("District deleted");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public StringResponse updateById(
            @PathVariable("id") Long id,
            @Valid @RequestBody Map<String, String> nameParam)
    {
        if (nameParam == null || nameParam.get("name") == null || nameParam.get("name").isBlank()) {
            throw new NotCorrectInput("Name must be given");
        }

        DistrictDto dto = new DistrictDto(id, nameParam.get("name"));
        districtService.patch(dto);
        return new StringResponse("District updated");
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public DistrictDto create(@RequestBody Map<String, String> nameParam){
        if (nameParam == null || nameParam.get("name") == null || nameParam.get("name").isBlank()) {
            throw new NotCorrectInput("Name must be given");
        }
        return districtService.createDistrict(nameParam.get("name"));
    }

}
