package org.example.core.services.objects;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.GoodCreateDto;
import org.example.core.dto.getting.goods.GoodGetForUserDto;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.patching.GoodPatchDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.dictionaries.TagHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.mapping.goods.GoodGetForUserMapper;
import org.example.core.mapping.goods.GoodGetFullDtoMapper;
import org.example.core.models.Category;
import org.example.core.models.Good;
import org.example.core.models.Tag;
import org.example.core.models.Unit;
import org.example.core.models.types.GoodStatusFromModerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GoodService {
    private static final Logger logger = LogManager.getLogger(GoodService.class);

    private GoodHibImpl goodHib;
    private TagHibImpl tagHib;
    private CategoryHibImpl categoryHib;
    private UnitHibImpl unitHib;
    private GoodGetFullDtoMapper goodGetFullDtoMapper;
    private GoodGetForUserMapper goodGetForUserMapper;

    @Transactional
    public List<GoodGetForUserDto> findAllForUser (GoodFilter filters){
        if (filters.getCategoryIds() != null && !filters.getCategoryIds().isEmpty()){
            List<Long> allCategories = categoryHib.findAllChildCategoryIds(filters.getCategoryIds());
            filters.setCategoryIds(allCategories);
        }

        List<Good> goods= goodHib.findAllByFilters(filters, true);
        if (goods.isEmpty()) return List.of();

        List<GoodGetForUserDto> dtos= new ArrayList<>();
        for (Good good : goods){
            dtos.add(goodGetForUserMapper.toDto(good));
        }


        return dtos;

    }
    @Transactional
    public List<GoodGetFullDto> findAllForAnalyst(GoodFilter filters){
        if (filters.getCategoryIds() != null && !filters.getCategoryIds().isEmpty()){
            List<Long> allCategories = categoryHib.findAllChildCategoryIds(filters.getCategoryIds());
            filters.setCategoryIds(allCategories);
        }
        List<Good> goods = goodHib.findAllByFilters(filters,false);
        return listToDto(goods);

    }

    @Transactional
    public GoodGetFullDto getFullById(Long id){
        Good good = goodHib.findByIdFullVersion(id);
        if (good == null){
            throw new DoesNoeExist("Good does not exist with given credentials");
        }
        return goodGetFullDtoMapper.toDto(good);
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
        Good good = goodHib.findByIdFullVersion(id);
        if (good == null){
            throw new DoesNoeExist("Good do not exist with given credentials");
        }
        return goodGetForUserMapper.toDto(good);
    }

    @Transactional
    public GoodIdDto createGood(GoodCreateDto dto){
        Good good = new Good();
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()){
            List<Tag> tags = checkTagIds(dto.getTagIds());
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

        return new GoodIdDto(newGood.getId(), newGood.getName());

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

        Good good = goodHib.findByIdFullVersion(dto.getId());
        if (good == null){
            throw new DoesNoeExist("Good doe not exist with given credentials");
        }

        if (dto.getName()!=null){
            if (dto.getName().equalsIgnoreCase(good.getName())){
                throw new NotCorrectInput("Good already has this name");

            }
            good.setName(dto.getName());
        }


        if (dto.getDescription() != null){
            good.setDescription(dto.getDescription());
        }

        if (dto.getTagIds() !=null){
            if (good.getTags()!= null && dto.getTagIds().size() == good.getTags().size()){
                if (new HashSet<>(dto.getTagIds())
                        .containsAll(
                                good.getTags().stream().map(Tag::getId).toList()
                        )){
                    throw new NotCorrectInput("This good already has these tags");
                }
            }
            List<Tag> tags= !dto.getTagIds().isEmpty()
                    ? checkTagIds(dto.getTagIds())
                    : Collections.emptyList();
            good.setTags(tags);
        }

        if (dto.getCategoryId() != null){
            Category category = categoryHib.findById(dto.getCategoryId(), logger);
            if (category == null){
                throw new DoesNoeExist("Category does not exist with given credentials");
            }
            if (good.getCategory()!= null && Objects.equals(good.getCategory().getId(), dto.getCategoryId())){
                throw new NotCorrectInput("This good already has this category");
            }
            good.setCategory(category);
        }

        if (dto.getUnitId() != null){
            Unit unit = unitHib.findById(dto.getUnitId(), logger);
            if (unit == null){
                throw new DoesNoeExist("Unit does not exist with given credentials");
            }
            if (Objects.equals(good.getUnit().getId(), dto.getUnitId())){
                throw new NotCorrectInput("This good already has this unit");
            }
            good.setUnit(unit);
        }

        good.setUpdatedAt(Instant.now());


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



    private Boolean hasAnyLetter(String str){
        // ^[\\p{L}]+$" - любые языки мира, \\p{L} - unicode Letter
        Pattern onlyLetters = Pattern.compile(".*\\p{L}.*");
        return str != null && onlyLetters.matcher(str).matches();

    }


}
