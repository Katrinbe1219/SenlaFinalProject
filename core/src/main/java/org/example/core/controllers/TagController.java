package org.example.core.controllers;

import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.TagDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.services.dictionaries.TagService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tags")
public class TagController {

    TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<TagDto> getAllTags(
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
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
        return tagService.getAllTags(count, page, filters, ids);

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public TagDto addTag(@RequestBody Map<String,String> nameParam){
        if (nameParam == null || nameParam.get("name") == null){
            throw new NotCorrectInput("Name must be given");
        }
        return tagService.createTag(nameParam.get("name"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public StringResponse deleteTag(@PathVariable("id") Long id){
        tagService.deleteTag(id);
        return new StringResponse("Tag deleted");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public StringResponse editTag(@PathVariable("id") Long id,
                                  @RequestBody Map<String, String> nameParam){
        if (nameParam == null || nameParam.get("name") == null || nameParam.get("name").isBlank()) {
            throw new NotCorrectInput("Name must be given");
        }

        TagDto dto = new TagDto();
        dto.setId(id);
        dto.setName(nameParam.get("name"));
        tagService.editTag(dto);
        return new StringResponse("Tag updated");
    }

    @GetMapping("/{id}")
    public TagDto getTag(@PathVariable("id") Long id){
        return tagService.getTagById(id);
    }



}
