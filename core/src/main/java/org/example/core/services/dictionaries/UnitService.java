package org.example.core.services.dictionaries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.UnitDto;
import org.example.core.dto.creating.UnitCreateDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.mapping.unit.UnitCreateDtoMapper;
import org.example.core.mapping.unit.UnitDtoMapper;
import org.example.core.models.Unit;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@DependsOn("liquibase")
@Service
public class UnitService {
    private static final Logger logger = LogManager.getLogger(UnitService.class);

    UnitHibImpl unitHib;
    private UnitDtoMapper mapper;
    private UnitCreateDtoMapper createDtoMapper;

    public UnitService(UnitHibImpl unitHib, UnitDtoMapper mapper, UnitCreateDtoMapper createDtoMapper) {
        this.mapper = mapper;
        this.unitHib = unitHib;
        this.createDtoMapper = createDtoMapper;
    }

    @Transactional
    public List<UnitDto> getAll(int count, int page, BaseSortTypes filters,List<Long> ids){
        List<Unit> units = unitHib.findAllWithSort(count, page,filters, ids, logger);
        if (units == null || units.isEmpty()) {
            return null;
        }
        return listToDto(units);
    }

    @Transactional
    public UnitDto getById(Long id){
        Unit unit= unitHib.findById(id, logger);
        if (unit == null){
            throw new DoesNoeExist("Unit with given credentials does not exist");
        }
        return mapper.toDto(unit);
    }

    @Transactional
    public void deleteById(Long id) throws NotCorrectInput{
        unitHib.delete(id, logger);
    }

    @Transactional
    public UnitDto create(UnitCreateDto unitDto){

        if ( ( unitDto.getFullName() != null && !isAlpha(unitDto.getFullName()) )
                ||
                (unitDto.getShortName() != null && !isAlpha(unitDto.getShortName()))
        ){
            throw new NotCorrectInput("Any name must contain only letters");
        }
        Unit unit = unitHib.save(createDtoMapper.toEntity(unitDto), logger);
        return mapper.toDto(unit);
    }

    @Transactional
    public void update(UnitDto unitDto){
        Unit unit = unitHib.findById(unitDto.getId(), logger);
        if (unit == null){
            throw new DoesNoeExist("Unit does not exist with given credentials");
        }
        if ( ( unitDto.getFullName() != null && !isAlpha(unitDto.getFullName()) )
                ||
                (unitDto.getShortName() != null && !isAlpha(unitDto.getShortName()))
        ){
            throw new NotCorrectInput("Any name must contain only letters");
        }


        if(unitDto.getFullName() != null && unitDto.getFullName().equalsIgnoreCase(unit.getFullName())){
            throw new NotCorrectInput("Unit already has this fullName");
        }

        if (unitDto.getShortName() != null && unitDto.getShortName().equalsIgnoreCase(unit.getShortName())){
            throw new NotCorrectInput("Unit already has this shortName");
        }

        if (unitDto.getFullName() != null){
            unit.setFullName(unitDto.getFullName());
        }

        if (unitDto.getShortName() != null){
            unit.setShortName(unitDto.getShortName());
        }

    }

    private List<UnitDto> listToDto(List<Unit> units) {
        List<UnitDto> dtos = new ArrayList<UnitDto>();
        for (Unit unit : units) {
            dtos.add(mapper.toDto(unit));
        }
        return dtos;
    }

    private Boolean isAlpha(String str){
        // ^[\\p{L}]+$" - любые языки мира, \\p{L} - unicode Letter
        Pattern onlyLetters = Pattern.compile("^[\\p{L}\\s]+$");
        return str != null && onlyLetters.matcher(str).matches();

    }
}
