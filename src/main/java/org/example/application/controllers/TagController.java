package org.example.application.controllers;

import org.example.application.dto.getting.StringResponse;
import org.example.application.dto.TagDto;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.services.dictionaries.TagService;
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
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page
    ){
        if (count <=0){
            throw new NotCorrectInput("Count must be greater than 0");
        }

        if (page <0) {
            throw new NotCorrectInput("Page must be >= 0");
        }
        return tagService.getAllTags(count, page);

    }

    @PostMapping
    public TagDto addTag(@RequestBody Map<String,String> nameParam){
        if (nameParam == null || nameParam.get("name") == null){
            throw new NotCorrectInput("Name must be given");
        }
        return tagService.createTag(nameParam.get("name"));
    }

    @DeleteMapping("/{id}")
    public StringResponse deleteTag(@PathVariable("id") Long id){
        tagService.deleteTag(id);
        return new StringResponse("Tag deleted");
    }

    @PatchMapping("/{id}")
    public StringResponse editTag(@PathVariable("id") Long id,
                                  @RequestBody Map<String, String> nameParam){
        if (nameParam == null || nameParam.get("name") == null) {
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
