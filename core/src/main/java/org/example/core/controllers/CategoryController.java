package org.example.core.controllers;

import jakarta.validation.Valid;
import org.example.core.dto.creating.CategoryCreateDto;
import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.patching.CategoryPatchDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.services.dictionaries.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
       this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryGetDto> getAll(
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value="sort", defaultValue = "1", required = false) Integer sort,
            @RequestParam(value="ids", required = false) List<Long> ids
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
        return categoryService.getAllCategories(count, page, filters, ids);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public CategoryGetDto createCategory(@RequestBody CategoryCreateDto categoryDto){
        return categoryService.createCategory(categoryDto);

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public StringResponse deleteCategory(@PathVariable("id") Long id){
        categoryService.deleteCategory(id);
        return new StringResponse("Category deleted");
    }

    @PatchMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public StringResponse editCategory(
                                       @Valid @RequestBody CategoryPatchDto dto){
        categoryService.patchCategory(dto);
        return new StringResponse("Category updated");
    }

    @GetMapping("/{id}")
    public CategoryGetDto getCategory(@PathVariable("id") Long id){
        return categoryService.getById(id);
    }
}
