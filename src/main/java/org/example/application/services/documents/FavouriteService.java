package org.example.application.services.documents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.application.dto.getting.favourites.FavouriteCountByGoodDto;
import org.example.application.dto.getting.favourites.FavouriteFullDto;
import org.example.application.dto.getting.favourites.FavouriteGetForUserDto;
import org.example.application.exceptions.CanNotMakeExecution;
import org.example.application.exceptions.DoesNoeExist;
import org.example.application.hibernate.documents.FavouriteHibImpl;
import org.example.application.hibernate.objects.GoodHibImpl;
import org.example.application.hibernate.objects.UserHibImpl;
import org.example.application.models.Favourite;
import org.example.application.models.Good;
import org.example.application.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class FavouriteService {
    private static final Logger logger = LogManager.getLogger(FavouriteService.class);

    private GoodHibImpl goodHib;
    private FavouriteHibImpl favouriteHib;
    private UserHibImpl userHib;
    // для авторизованных пользователей, если будет ошибка с поиском user по логину
    // какая-то проблема

    public FavouriteService(FavouriteHibImpl favouriteHib, UserHibImpl userHib, GoodHibImpl goodHib) {
        this.favouriteHib = favouriteHib;
        this.userHib = userHib;
        this.goodHib = goodHib;
    }

    @Transactional
    public List<FavouriteFullDto> findAllForAnalyst(){
        List<Favourite> favs = favouriteHib.findAllFullVersion();
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
    public List<FavouriteCountByGoodDto> countAllByGoodId(){
        return favouriteHib.countAllByGoodId();
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
