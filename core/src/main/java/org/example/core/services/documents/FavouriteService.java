package org.example.core.services.documents;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.favourites.FavouriteCountByGoodDto;
import org.example.core.dto.getting.favourites.FavouriteFullDto;
import org.example.core.dto.getting.favourites.FavouriteGetForUserDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.hibernate.base_settings.filters.FavouritesAnalystFilters;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.documents.FavouriteHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.models.Category;
import org.example.core.models.Favourite;
import org.example.core.models.Good;
import org.example.core.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FavouriteService {
    private static final Logger logger = LogManager.getLogger(FavouriteService.class);
    private final CategoryHibImpl categoryHibImpl;

    private GoodHibImpl goodHib;
    private FavouriteHibImpl favouriteHib;
    private UserHibImpl userHib;
    // для авторизованных пользователей, если будет ошибка с поиском user по логину
    // какая-то проблема


    @Transactional
    public List<FavouriteFullDto> findAllForAnalyst(  FavouritesAnalystFilters filters){

        if (filters.getCategoryIds()!=null ){
            List<Long> allCat = categoryHibImpl.findAllChildCategoryIds(filters.getCategoryIds());
            filters.setCategoryIds(allCat);
        }

        List<Favourite> favs = favouriteHib.findAllFullVersion(filters);
        return listToDto(favs);
    }

    @Transactional
    public List<FavouriteGetForUserDto> getAllForUser(String login){

        Long id = userHib.getUserIdByLogin(login);
        if (id == null){
            throw new CanNotMakeExecution("FavouriteService getAllForUser - не найден пользователь по login: " + login);
        }
        return favouriteHib.findAllByUser(id);
    }

    @Transactional
    public FavouriteGetForUserDto getForUser(String login, Long goodId){
        Long id = userHib.getUserIdByLogin(login);
        if (id == null){
            throw new CanNotMakeExecution("FavouriteService getAllForUser - не найден пользователь по login: " + login);
        }
        return favouriteHib.findByUserIdAndGoodId(id, goodId);
    }



    @Transactional
    public List<FavouriteCountByGoodDto> countAllByGoodId(FavouritesAnalystFilters filters){
        if (filters.getCategoryIds()!=null ){
            List<Long> allCat = categoryHibImpl.findAllChildCategoryIds(filters.getCategoryIds());
            filters.setCategoryIds(allCat);
        }
        return favouriteHib.countAllByGoodId(filters);
    }

    @Transactional
    public FavouriteCountByGoodDto countOneByGoodId(Long goodId){
        FavouriteCountByGoodDto fav = favouriteHib.countByGoodId(goodId);
        if (fav == null){
            throw new DoesNoeExist("Favourite does not exist with given credentials");
        }
        return fav;
    }

    @Transactional
    public void createFavourite(String login, Long goodId){
        User user = userHib.getByLoginSmallVersion(login);
        if (user == null){
            throw new CanNotMakeExecution("FavouriteService getAllForUser - не найден пользователь по login: " + login);
        }

        Good good = goodHib.findById(goodId, logger);
        if (good == null){
            throw new DoesNoeExist("Good does not exist with given credentials");
        }

        Favourite fav = new Favourite();
        fav.setCreatedAt(Instant.now());
        fav.setUser(user);
        fav.setGood(good);
        favouriteHib.save(fav, logger);
    }

    @Transactional
    public void removeFavourite(String login, Long goodId){
        Long id = userHib.getUserIdByLogin(login);
        if (id == null){
            throw new CanNotMakeExecution("FavouriteService getAllForUser - не найден пользователь по login: " + login);
        }

        Favourite fav = favouriteHib.findByUserIdAndGoodIdPureVersion(id, goodId);
        if (fav == null){
            throw new DoesNoeExist("Favourite does not exist with given credentials");
        }

        favouriteHib.remove(fav);
    }

    private FavouriteFullDto toDto(Favourite favourite){
        FavouriteFullDto dto = new FavouriteFullDto();
        dto.setId(favourite.getId());
        dto.setCreatedAt(favourite.getCreatedAt());
        dto.setGoodId(favourite.getGood().getId());
        dto.setGoodName(favourite.getGood().getName());
        dto.setUserId(favourite.getUser().getId());
        return dto;
    }

    private List<FavouriteFullDto> listToDto(List<Favourite> old){
        List<FavouriteFullDto> dtos = new ArrayList<>();
        for (Favourite favourite : old){
            dtos.add(toDto(favourite));
        }
        return dtos;
    }


}
