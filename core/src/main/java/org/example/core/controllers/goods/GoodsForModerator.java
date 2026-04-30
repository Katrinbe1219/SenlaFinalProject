package org.example.core.controllers.goods;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.StringRequest;
import org.example.core.dto.creating.GoodCreateDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.goods.GoodGetForUserDto;
import org.example.core.dto.patching.GoodPatchDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.models.User;
import org.example.core.models.types.ModeratorVerdict;
import org.example.core.services.documents.ModeratorRecalcService;
import org.example.core.services.objects.GoodService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/moderator/goods")
@AllArgsConstructor
public class GoodsForModerator {

    private GoodService goodService;
    private ModeratorRecalcService logService;

    @PostMapping
    public GoodGetForUserDto createGood(
            @Valid @RequestBody GoodCreateDto dto){
        return goodService.createGood(dto);
    }

    @DeleteMapping("/{id}")
    public StringResponse delete(@PathVariable("id") Long id){
        goodService.delete(id);
        return new StringResponse("Good was deleted");
    }

    @PatchMapping("/{id}")
    public StringResponse patch(@PathVariable("id") Long id,
                                @Valid @RequestBody GoodPatchDto dto){

        dto.setId(id);
        goodService.patch(dto);
        return new StringResponse("Good was updated");
    }

    @PatchMapping("/{id}/block")
    public StringResponse block(@PathVariable("id") Long id,
                                @RequestBody StringRequest request){

        if (request.getMessage() == null || request.getMessage().isBlank()){
            throw new NotCorrectInput("Comment must be given");
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        logService.addLog(user, id, ModeratorVerdict.SUSPICIOUS, request.getMessage());
        return new StringResponse("Good was blocked");
    }

    @DeleteMapping("/{id}/block")
    public StringResponse unblock(@PathVariable("id") Long id,
                                @RequestParam("verdict") int verdictInt,
                                @RequestBody StringRequest request){

        if (request.getMessage() == null || request.getMessage().isBlank()){
            throw new NotCorrectInput("Comment must be given");
        }

        ModeratorVerdict verdict = ModeratorVerdict.forValue(verdictInt);


        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        logService.addLog(user, id, verdict, request.getMessage());
        return new StringResponse("Good was unblocked");
    }


}
