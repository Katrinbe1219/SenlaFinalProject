package org.example.application.services.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.application.dto.creating.GoodCreateDto;
import org.example.application.dto.getting.goods.GoodGetForUserDto;
import org.example.application.dto.getting.goods.GoodGetFullDto;
import org.example.application.dto.patching.GoodPatchDto;
import org.example.application.exceptions.DoesNoeExist;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.application.hibernate.dictionaries.CategoryHibImpl;
import org.example.application.hibernate.dictionaries.TagHibImpl;
import org.example.application.hibernate.dictionaries.UnitHibImpl;
import org.example.application.hibernate.objects.GoodHibImpl;
import org.example.application.mapping.goods.GoodGetFullDtoMapper;
import org.example.application.models.Category;
import org.example.application.models.Good;
import org.example.application.models.Tag;
import org.example.application.models.Unit;
import org.example.application.models.types.GoodStatusFromModerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GoodService {
    private static final Logger logger = LogManager.getLogger(GoodService.class);

    private GoodHibImpl goodHib;
    private TagHibImpl tagHib;
    private CategoryHibImpl categoryHib;
    private UnitHibImpl unitHib;
    private GoodGetFullDtoMapper goodGetFullDtoMapper;

    public GoodService(GoodHibImpl goodHib, TagHibImpl tagHib, CategoryHibImpl categoryHib, UnitHibImpl unitHib,
                       GoodGetFullDtoMapper goodGetFullDtoMapper) {
        this.goodHib = goodHib;
        this.tagHib = tagHib;
        this.categoryHib = categoryHib;
        this.unitHib = unitHib;
        this.goodGetFullDtoMapper = goodGetFullDtoMapper;
    }

    @Transactional
    public List<GoodGetForUserDto> findAllForUser (GoodFilter filters){
        if (filters.getCategoryIds() != null && !filters.getCategoryIds().isEmpty()){
            List<Long> allCategories = categoryHib.findAllChildCategoryIds(filters.getCategoryIds());
            filters.setCategoryIds(allCategories);
        }


        return goodHib.findAllForUserDto(filters);

    }
    @Transactional
    public List<GoodGetFullDto> findAllForAnalyst(GoodFilter filters){
        if (filters.getCategoryIds() != null && !filters.getCategoryIds().isEmpty()){
            List<Long> allCategories = categoryHib.findAllChildCategoryIds(filters.getCategoryIds());
            filters.setCategoryIds(allCategories);
        }
        List<Good> goods = goodHib.findAllForAnalyst(filters);
        return listToDto(goods);

    }

    private List<GoodGetFullDto> listToDto(List<Good> goods){
        List<GoodGetFullDto> dtos = new ArrayList<>();
        for (Good good : goods){
            dtos.add(goodGetFullDtoMapper.toDto(good));
        }
        return dtos;
    }

    @Transactional
    public GoodGetForUserDto findForUserById(Long id){
        GoodGetForUserDto good = goodHib.getGoodForUserById(id);
        if (good == null){
            throw new DoesNoeExist("Good do not exist with given credentials");
        }
        return good;
    }

    @Transactional
    public GoodGetForUserDto createGood(GoodCreateDto dto){
        Good good = new Good();
        if (dto.getTagsId() != null && !dto.getTagsId().isEmpty()){
            List<Tag> tags = checkTagIds(dto.getTagsId());
            good.setTags(tags);
        }

        good.setName(dto.getName());
        good.setDescription(dto.getDescription());
        good.setRate(BigDecimal.ZERO.doubleValue());
        good.setCreatedAt(Instant.now());
        good.setUpdatedAt(Instant.now());
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);

        if (dto.getCategoryId() != null){
            Category category = categoryHib.findById(dto.getCategoryId(), logger);
            if (category == null){
                throw new DoesNoeExist("Category does not exist with given credentials");
            }
            good.setCategory(category);
        }

        Unit unit = unitHib.findById(dto.getUnitId(), logger);
        if (unit == null){
            throw new DoesNoeExist("Unit does not exist with given credentials");
        }
        good.setUnit(unit);
        Good newGood = goodHib.save(good, logger);

        return toDto(newGood);

    }


    @Transactional
    public void delete(Long id){
        goodHib.delete(id, logger);
    }

    @Transactional
    public void patch(GoodPatchDto dto){
        if (dto.getName() != null && !hasAnyLetter(dto.getName())){
            throw new NotCorrectInput("Name must contain letters");
        }

        Good good = goodHib.findById(dto.getId(), logger);
        if (good == null){
            throw new DoesNoeExist("Good doe not exist with given credentials");
        }

        if (dto.getName()!=null){
            good.setName(dto.getName());
        }


        if (dto.getDescription() != null){
            good.setDescription(dto.getDescription());
        }

        if (dto.getTagIds() !=null && !dto.getTagIds().isEmpty()){
            List<Tag> tags= checkTagIds(dto.getTagIds());
            good.setTags(tags);
        }

        if (dto.getCategoryId() != null){
            Category category = categoryHib.findById(dto.getCategoryId(), logger);
            if (category == null){
                throw new DoesNoeExist("Category does not exist with given credentials");
            }
            good.setCategory(category);
        }

        if (dto.getUnitId() != null){
            Unit unit = unitHib.findById(dto.getUnitId(), logger);
            if (unit == null){
                throw new DoesNoeExist("Unit does not exist with given credentials");
            }
            good.setUnit(unit);
        }


    }


    private List<Tag> checkTagIds(List<Long> ids){
        List<Long> idsSorted = ids.stream().distinct().toList();
        List<Tag> tags = tagHib.findAllById(idsSorted);

        if (tags.size() != idsSorted.size()){
            Set<Long> foundIds = tags.stream().map(Tag::getId).collect(Collectors.toSet());
            List<Long> missingIds = idsSorted.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new DoesNoeExist("Tags are not valid: " + missingIds);
        }
        return tags;
    }

    private GoodGetForUserDto toDto(Good good){
        GoodGetForUserDto dto = new GoodGetForUserDto();
        dto.setId(good.getId());
        dto.setName(good.getName());
        if (good.getCategory() !=null){
            dto.setCategory(good.getCategory().getName());
        }

        dto.setUnit(good.getUnit().getFullName());
        if (good.getTags() != null){
            dto.setTags(good.getTags().stream().map(Tag::getName).collect(Collectors.joining(", ")));
        }
        return dto;
    }

    private Boolean hasAnyLetter(String str){
        // ^[\\p{L}]+$" - любые языки мира, \\p{L} - unicode Letter
        Pattern onlyLetters = Pattern.compile(".*\\p{L}.*");
        return str != null && onlyLetters.matcher(str).matches();

    }


}
