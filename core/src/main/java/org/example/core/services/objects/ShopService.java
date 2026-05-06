package org.example.core.services.objects;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.ShopCreateDto;
import org.example.core.dto.getting.statistics.shops.ShopGetDto;
import org.example.core.dto.patching.ShopPatchDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.mapping.ShopGetMapper;
import org.example.core.models.District;
import org.example.core.models.Shop;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class ShopService {
    private static final Logger logger = LogManager.getLogger(ShopService.class);

    private ShopHibImpl shopHib;
    private DistrictHibImpl districtHib;
    private ShopGetMapper mapper;


    @Transactional
    public List<ShopGetDto> findAll(Integer count, Integer page, BaseSortTypes filters,
                                    List<Long> ids, List<Long> districtIds){
        List<Shop> shops = shopHib.findAllFullVersion(count, page, filters, ids, districtIds);
        return listToDto(shops);
    }

    @Transactional
    public ShopGetDto findById(Long id) {
        Shop shop = shopHib.findByIdFullVersion(id);
        if (shop == null) {
            throw new DoesNoeExist("Shop does not exist with given credentials");
        }

        return mapper.toDto(shop);
    }

    @Transactional
    public ShopGetDto create(ShopCreateDto shopCreateDto) {
        if (!isAlpha(shopCreateDto.getName()) ) {
            throw new NotCorrectInput("Name must contain only letters");
        }

        District district = districtHib.findById(shopCreateDto.getDistrictId(), logger);
        if (district == null) {
            throw new DoesNoeExist("District does not exist with given credentials");
        }

        Shop shop = toEntity(shopCreateDto, district);
        return mapper.toDto(shopHib.save(shop, logger));
    }

    @Transactional
    public void delete(Long id){
        shopHib.delete(id, logger);
    }

    @Transactional
    public void patch(ShopPatchDto dto){

        if (dto.getName() != null && !isAlpha(dto.getName())){
            throw new NotCorrectInput("Name must contain only letters");
        }

        if (dto.getAddress() != null && !isAlpha(dto.getAddress())){
            throw new NotCorrectInput("Address must contain only letters");
        }

        Shop old = shopHib.findById(dto.getId(), logger);
        if (old == null) {
            throw new DoesNoeExist("Shop does not exist with given credentials");
        }


        District district = districtHib.findById(dto.getDistrictId(), logger);
        if (district == null) {
            throw new DoesNoeExist("District does not exist with given credentials");
        }

        Shop newShop = toEntity(dto, district);

        shopHib.update(newShop, logger);


    }



    private List<ShopGetDto> listToDto(List<Shop> shops) {
        List<ShopGetDto> shopGetDtos = new ArrayList<>();
        for (Shop shop : shops) {
            shopGetDtos.add(mapper.toDto(shop));
        }
        return shopGetDtos;
    }
    private Shop toEntity(ShopCreateDto old, District district){
        Shop shop = new Shop();
        shop.setAddress(old.getAddress());
        shop.setName(old.getName());
        shop.setDistrict(district);
        return shop;
    }

    private Shop toEntity(ShopPatchDto old, District district){
        Shop shop = new Shop();
        shop.setAddress(old.getAddress());
        shop.setName(old.getName());
        shop.setDistrict(district);
        return shop;
    }

    private Boolean isAlpha(String str){
        // ^[\\p{L}]+$" - любые языки мира, \\p{L} - unicode Letter
        Pattern onlyLetters = Pattern.compile("^[\\p{L}\\s]+$");
        return str != null && onlyLetters.matcher(str).matches();

    }



}
