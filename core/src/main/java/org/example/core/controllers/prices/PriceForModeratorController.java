package org.example.core.controllers.prices;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.prices.PriceGetResultForModerator;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.prices.PriceFilter;
import org.example.core.services.documents.prices.PriceService;
import org.example.core.services.documents.prices.data.OptionForUpload;
import org.example.core.services.upload.ImportFileCsvService;
import org.example.core.services.upload.ImportFileXlsxService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/moderator/prices")
@RequiredArgsConstructor
public class PriceForModeratorController {

    // delete - при остановке продажи какого-то товара изменяется поле валидности товара
    private final PriceService priceService;
    private final ImportFileCsvService importFileCsvService;
    private final ImportFileXlsxService importFileXlsxService;

    @PostMapping
    public PriceGetResultForModerator createPriceForGoodInShop(@Valid  @RequestBody PriceCreateDto dto) {
        return priceService.createPrice(dto);
    }

    @PatchMapping("/update")
    // чисто обновление, подгрузка категории необязательна в результате
    public PriceGetResultForModerator updatePriceForGoodInShop(@Valid @RequestBody PriceCreateDto dto) {
        return priceService.updatePrice(dto);
    }

    @PostMapping("/upload")
    public StringResponse uploadFile(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "option", required = false, defaultValue = "stop") String option,
                                     @RequestParam(value = "send", required = true, defaultValue = "false" ) Boolean isSend){

        OptionForUpload optionForUpload = OptionForUpload.getValue(option);
        if (isSend && optionForUpload == OptionForUpload.SKIP) {
            throw new NotCorrectInput("Option skip can not be applied with parameter send = true");
        }
        if (file.isEmpty()){
            throw new NotCorrectInput("File is empty!");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null ||  (
                !originalName.endsWith(".csv") && !originalName.endsWith(".xls") && !originalName.endsWith(".xlsx"))){
            throw new NotCorrectInput("File is wrong! It is either csv or xls or xlsx");
        }

        List<PriceCreateDto> dtos;
        try{
            if (originalName.endsWith(".csv")){
                dtos = importFileCsvService.importPrices(file.getInputStream());
                priceService.saveAll(dtos, optionForUpload, isSend);
            }else if (originalName.endsWith(".xls") || originalName.endsWith(".xlsx")){
                dtos = importFileXlsxService.importPrices(file.getInputStream());
                priceService.saveAll(dtos, optionForUpload, isSend);
            }
        }catch (NotCorrectInput e){
            throw e;
        }
        catch (Exception e){
            throw new NonHibernateException("PriceForModeratorController uploadFile: File can not be open: " + e.getMessage());
        }


        return new StringResponse("Everything was uploaded");
    }

    @GetMapping
    public List<PriceGetResultForModerator> getPrices(
            @Valid @RequestBody PriceFilter filters
    ){
        return priceService.getByFilters(filters);

    }

    @DeleteMapping
    public StringResponse deletePrice(
            @RequestParam("goodId") Long goodId,
            @RequestParam("shopId") Long shopId
    ){
        if (goodId <=0){
            throw new NotCorrectInput("goodId must be >0 ");
        }
        if (shopId <=0){
            throw new NotCorrectInput("shopId must be >0 ");
        }
        priceService.deletePriceByGoodAndShop(goodId, shopId);
        return new StringResponse("Price was deleted successfully");
    }

    @DeleteMapping("/{id}")
    public StringResponse deletePrice(@PathVariable("id") Long id){
        if (id <=0){
            throw new NotCorrectInput("id must be >0 ");
        }
        priceService.deletePriceById(id);
        return new StringResponse("Price was deleted successfully");
    }

    @GetMapping("/{id}")
    public PriceGetResultForModerator getPrice(@PathVariable("id") Long id){
        if (id <=0){
            throw new NotCorrectInput("id must be >0 ");
        }
        return priceService.getByIdForModerator(id);
    }

}
