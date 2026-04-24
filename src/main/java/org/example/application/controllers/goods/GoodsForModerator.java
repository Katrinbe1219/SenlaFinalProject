package org.example.application.controllers.goods;

import lombok.AllArgsConstructor;
import org.example.application.dto.StringRequest;
import org.example.application.dto.creating.GoodCreateDto;
import org.example.application.dto.getting.StringResponse;
import org.example.application.dto.getting.goods.GoodGetForUserDto;
import org.example.application.dto.patching.GoodPatchDto;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.models.User;
import org.example.application.models.types.ModeratorVerdict;
import org.example.application.services.documents.ModeratorRecalcService;
import org.example.application.services.objects.GoodService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goods")
@AllArgsConstructor
public class GoodsForModerator {

    private GoodService goodService;
    private ModeratorRecalcService logService;

    @PostMapping
    public GoodGetForUserDto createGood(@RequestBody GoodCreateDto dto){
        if (dto.getName() == null || dto.getName().isEmpty()){
            throw new NotCorrectInput("Name must be given");
        }

        if (dto.getUnitId() == null ){
            throw new NotCorrectInput("Unit must be given");
        }
        return goodService.createGood(dto);


    }

    @DeleteMapping("/{id}")
    public StringResponse delete(@PathVariable("id") Long id){
        goodService.delete(id);
        return new StringResponse("Good was deleted");
    }

    @PatchMapping("/{id}")
    public StringResponse patch(@PathVariable("id") Long id,
                                @RequestBody GoodPatchDto dto){
        // status изменяться здесь не может, для этого будет отдельная функция с логами дополнительными
        if (dto.getName() != null && dto.getName().isEmpty()){
            throw new NotCorrectInput("Name must be given");
        }
        dto.setId(id);
        goodService.patch(dto);
        return new StringResponse("Good was updated");
    }

    @PatchMapping("/{id}/block")
    public StringResponse block(@PathVariable("id") Long id,
                                @RequestBody StringRequest request){

        if (request.getMessage() == null || request.getMessage().isEmpty()){
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

        if (request.getMessage() == null || request.getMessage().isEmpty()){
            throw new NotCorrectInput("Comment must be given");
        }

        ModeratorVerdict verdict = ModeratorVerdict.forValue(verdictInt);


        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        logService.addLog(user, id, verdict, request.getMessage());
        return new StringResponse("Good was unblocked");
    }


}
