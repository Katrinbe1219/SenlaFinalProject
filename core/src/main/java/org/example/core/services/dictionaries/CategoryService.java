package org.example.core.services.dictionaries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.CategoryCreateDto;
import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.dto.patching.CategoryPatchDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.mapping.categories.CategoryGetDtoMapper;
import org.example.core.models.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class CategoryService {

    private static  final Logger logger = LogManager.getLogger(CategoryService.class);

    CategoryHibImpl categoryHib;
    private CategoryGetDtoMapper categoryGetDtoMapper;
    public CategoryService(CategoryHibImpl categoryHib, CategoryGetDtoMapper categoryGetDtoMapper) {
        this.categoryHib = categoryHib;
        this.categoryGetDtoMapper = categoryGetDtoMapper;
    }

    @Transactional
    public List<CategoryGetDto> getAllCategories(Integer count, Integer page, BaseSortTypes filters, List<Long> ids) {
        return  categoryHib.findAllFullVersion(count, page, filters, ids);
    }

    @Transactional
    public CategoryGetDto createCategory( CategoryCreateDto categoryCreateDto) {

        Category parentCheck = null;
        if (categoryCreateDto.getParentId() != null){
            parentCheck = categoryHib.findById(categoryCreateDto.getParentId(), logger);
            if (parentCheck == null){
                throw new DoesNoeExist("Parent not found");
            }
        }
        if (!isAlpha(categoryCreateDto.getName())){
            throw new NotCorrectInput("Name must contain only letters");
        }

        try{
            Category newCategory = toEntity(categoryCreateDto, parentCheck);
            return categoryGetDtoMapper.toCategoryGetDto(categoryHib.save(newCategory, logger));
        }catch (Exception e){
            logger.error("CategoryService  createCategory: "+e.getMessage());
            throw e;
        }

    }

    @Transactional
    public void deleteCategory(Long id){
        try{
            categoryHib.delete(id, logger);
        }catch (Exception e){
            logger.error("CategoryService  deleteCategory: "+e.getMessage());
        }

    }

    @Transactional
    public void patchCategory(CategoryPatchDto dto){
        Category parentCheck = null;
        if (dto.getParentId() != null){
            parentCheck= categoryHib.findById(dto.getParentId(), logger);
            if (parentCheck == null){
                throw new DoesNoeExist("Parent not found");
            }
        }

        if (dto.getName()!= null && !isAlpha(dto.getName())){
            throw new NotCorrectInput("Name must contain only letters");
        }



        Category cat = toEntity(dto, parentCheck);
        categoryHib.update(cat);

    }

    @Transactional
    public CategoryGetDto getById(Long id) throws DoesNoeExist {
        Category category = categoryHib.findById(id, logger);
        if (category == null){
            throw new DoesNoeExist("Category with id: " + id + " not found");
        }
        return categoryGetDtoMapper.toCategoryGetDto(category);
    }

    private Category toEntity(CategoryCreateDto categoryDto, Category parent) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setParent(parent);
        return category;
    }


    private Category toEntity(CategoryPatchDto old, Category parent){
        Category category = new Category();
        category.setName(old.getName());
        category.setParent(parent);
        category.setId(old.getId());
        return category;
    }
    private Boolean isAlpha(String str){
        // ^[\\p{L}]+$" - любые языки мира, \\p{L} - unicode Letter
        Pattern onlyLetters = Pattern.compile("^[\\p{L}\\s]+$");
        return str != null && onlyLetters.matcher(str).matches();

    }
}
