package org.example.core.services.dictionaries;

import org.example.core.dto.DistrictDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.models.District;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class DistrictService {
    private final static Logger logger = LogManager.getLogger(DistrictService.class);
    DistrictHibImpl districtHib;

    public DistrictService(DistrictHibImpl districtHib) {
        this.districtHib = districtHib;
    }
    @Transactional
    public DistrictDto createDistrict(String name){
        District district = new District();
        district.setName(name);
        return toDto(districtHib.save(district, logger));
    }
    @Transactional
    public void deleteDistrict(Long id) throws NotCorrectInput {
        districtHib.delete(id, logger);

    }

    @Transactional
    public void patch(DistrictDto dto){
        if (districtHib.findById(dto.getId(), logger) == null){
            throw new DoesNoeExist("District does not exist with given credentials");
        }
        if (!isAlpha(dto.getName())){
            throw new NotCorrectInput("District must contain only letters");
        }

        districtHib.update(toEntity(dto), logger);
    }


    @Transactional
    public DistrictDto getById(Long id){
        District dist = districtHib.findById(id, logger);
        if (dist ==null){
            throw new DoesNoeExist("District does not exist with given credentials");
        }
        if (!isAlpha(dist.getName())){
            throw new NotCorrectInput("District must contain only letters");
        }
        return toDto(dist);
    }

    @Transactional
    public List<DistrictDto> getAll(int count, int page, BaseSortTypes filters, List<Long> ids){

        List<District> dists = districtHib.findAllWithSort(count, page,filters, ids, logger);
        if (dists == null || dists.isEmpty()){
            return null;
        }
        List<DistrictDto> dtos = new ArrayList<>();
        for (District district : dists) {
            dtos.add(toDto(district));
        }

        return dtos;
    }



    public District toEntity(DistrictDto dto){
        District district = new District();
        district.setName(dto.getName());
        district.setId(dto.getId());
        return district;
    }


    public DistrictDto toDto(District district){
        DistrictDto dto = new DistrictDto();
        dto.setId(district.getId());
        dto.setName(district.getName());
        return dto;
    }

    private Boolean isAlpha(String str){
        // ^[\\p{L}]+$" - любые языки мира, \\p{L} - unicode Letter
        Pattern onlyLetters = Pattern.compile("^[\\p{L}\\s]+$");
        return str != null && onlyLetters.matcher(str).matches();

    }
}
