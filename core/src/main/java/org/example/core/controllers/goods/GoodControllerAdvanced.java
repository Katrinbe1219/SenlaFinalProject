package org.example.core.controllers.goods;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.StringRequest;
import org.example.core.dto.creating.GoodCreateDto;
import org.example.core.dto.creating.ManyModeratorLogCreateDto;
import org.example.core.dto.creating.ModeratorLogCreateDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.patching.GoodPatchDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.models.User;
import org.example.core.models.types.ModeratorVerdict;
import org.example.core.services.documents.ModeratorRecalcService;
import org.example.core.services.objects.GoodService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods/advanced")
@AllArgsConstructor
public class GoodControllerAdvanced {

    private GoodService goodService;
    private ModeratorRecalcService logService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'MODERATOR')")
    public List<GoodGetFullDto> findAllAnalyst(
            @Valid @RequestBody GoodFilter filters
    ){
        return goodService.findAllForAnalyst(filters);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'MODERATOR')")
    public GoodGetFullDto findById(@PathVariable("id")Long id){
        if (id <=0){
            throw new NotCorrectInput("id must be > 0");
        }

        return goodService.getFullById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public GoodIdDto createGood(
            @Valid @RequestBody GoodCreateDto dto){
        return goodService.createGood(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public StringResponse delete(@PathVariable("id") Long id){
        goodService.delete(id);
        return new StringResponse("Good was deleted");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public StringResponse patch(@PathVariable("id") Long id,
                                @Valid @RequestBody GoodPatchDto dto){

        dto.setId(id);
        goodService.patch(dto);
        return new StringResponse("Good was updated");
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public StringResponse block(@PathVariable("id") Long id,
                                @RequestBody ModeratorLogCreateDto request){


        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        logService.addLog(user, id, ModeratorVerdict.SUSPICIOUS, request.getComment());
        return new StringResponse("Good was blocked");
    }

    @DeleteMapping("/{id}/block")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public StringResponse unblock(@PathVariable("id") Long id,
                                  @Valid  @RequestBody ModeratorLogCreateDto dto){

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        logService.addLog(user, id, dto.getVerdict(), dto.getComment());
        return new StringResponse("Good was unblocked");
    }


}
