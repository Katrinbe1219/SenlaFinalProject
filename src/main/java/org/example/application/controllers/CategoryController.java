package org.example.application.controllers;

import org.example.application.dto.creating.CategoryCreateDto;
import org.example.application.dto.getting.CategoryGetDto;
import org.example.application.dto.getting.StringResponse;
import org.example.application.dto.patching.CategoryPatchDto;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.services.dictionaries.CategoryService;
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
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page
    ){
        if (count <=0){
            throw new NotCorrectInput("Count must be greater than 0");
        }

        if (page <0) {
            throw new NotCorrectInput("Page must be >= 0");
        }
        return categoryService.getAllCategories(count, page);
    }

    @PostMapping
    public CategoryGetDto createCategory(@RequestBody CategoryCreateDto categoryDto){
        if (categoryDto.getName() == null){
            throw new NotCorrectInput("Name must be given");
        }
        return categoryService.createCategory(categoryDto);

    }

    @DeleteMapping("/{id}")
    public StringResponse deleteCategory(@PathVariable("id") Long id){
        categoryService.deleteCategory(id);
        return new StringResponse("Category deleted");
    }

    @PatchMapping("/{id}")
    public StringResponse editCategory(@PathVariable("id") Long id,
                                       @RequestBody CategoryPatchDto dto){
        dto.setId(id);
        categoryService.patchCategory(dto);
        return new StringResponse("Category updated");
    }

    @GetMapping("/{id}")
    public CategoryGetDto getCategory(@PathVariable("id") Long id){
        return categoryService.getById(id);
    }
}
