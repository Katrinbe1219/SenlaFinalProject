package org.example.core.services.dictionaries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.TagDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.hibernate.dictionaries.TagHibImpl;
import org.example.core.mapping.TagDtoMapper;
import org.example.core.models.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TagService {
    private static final Logger logger = LogManager.getLogger(TagService.class);

    private TagHibImpl tagHibImpl;
    private TagDtoMapper mapper;

    public TagService(TagHibImpl tagHibImpl, TagDtoMapper mapper) {
        this.mapper = mapper;
        this.tagHibImpl = tagHibImpl;
    }

    @Transactional
    public List<TagDto> getAllTags(Integer count, Integer page, BaseSortTypes filters,List<Long> ids){
        List<Tag> tags = tagHibImpl.findAllWithSort(count, page,filters,ids, logger);
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return listToDto(tags);
    }

    @Transactional
    public TagDto createTag(String name){
        if (!isAlpha(name)) {
            throw new NotCorrectInput("Tag must contain only letters");
        }

        Tag tag = new Tag();
        tag.setName(name);
        return mapper.toDto(tagHibImpl.save(tag, logger));
    }

    @Transactional
    public void deleteTag(Long id){
        tagHibImpl.delete(id, logger);
    }

    @Transactional
    public void editTag(TagDto tagUpdated) throws NotCorrectInput {
        if (tagUpdated.getId() == null
                || tagUpdated.getId() == 0 || tagUpdated.getName() == null) {
            throw new NotCorrectInput("Введите все поля: новое название, id тега");
        }

        if (!isAlpha(tagUpdated.getName())) {
            throw new NotCorrectInput("Tag must contain only letters");
        }
        //Tag tag = toEntity(tagUpdated);
        tagHibImpl.update(tagUpdated); // без merge
    }

    @Transactional
    public TagDto getTagById(Long id){
        Tag tag = tagHibImpl.findById(id, logger);
        if (tag == null) {
            throw new DoesNoeExist("Tag does not exist with given credentials");
        }
        return mapper.toDto(tag);
    }

    private Boolean isAlpha(String str){
        // ^[\\p{L}]+$" - любые языки мира, \\p{L} - unicode Letter
        Pattern onlyLetters = Pattern.compile("^[\\p{L}\\s]+$");
        return str != null && onlyLetters.matcher(str).matches();

    }

    private List<TagDto> listToDto(List<Tag> tags){
        List<TagDto> dtos = new ArrayList<>();
        for( Tag tag : tags ){
            dtos.add(mapper.toDto(tag));
        }
        return dtos;
    }

}
