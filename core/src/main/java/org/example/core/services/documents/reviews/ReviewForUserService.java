package org.example.core.services.documents.reviews;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.ReviewCreateDto;
import org.example.core.dto.getting.reviews.ReviewDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.exceptions.PermissionDenied;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewForUserFilters;
import org.example.core.hibernate.documents.ReviewHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.mapping.reviews.ReviewForUserDtoMapper;
import org.example.core.models.Good;
import org.example.core.models.Review;
import org.example.core.models.User;
import org.example.core.models.types.GoodStatusFromModerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ReviewForUserService {
    private static final Logger logger = LogManager.getLogger(ReviewForUserService.class);

    ReviewHibImpl reviewHib;
    UserHibImpl userHib;
    GoodHibImpl goodHib;
    ReviewForUserDtoMapper mapper;


    @Transactional
    public List<ReviewDto> getByUserId(String username, int page, int size){
        try{
            Long userId = userHib.getUserIdByLogin(username);
            if (userId == null){
                throw new NotCorrectInput("Пользователь был не найден");
            }
            List<Review> reviews=  reviewHib.getByUserSmallVersion(userId, page, size);
            if (reviews.isEmpty()) return List.of();
            return listToDto(reviews);

        }catch(NotCorrectInput e){
            throw e;
        }
        catch (Exception e){
            logger.error("ReviewForUserService getByUserId:" + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public ReviewDto getByUserAndGood(String username, Long good){
        try{
            Long userId = userHib.getUserIdByLogin(username);
            if (userId == null){
                throw new NotCorrectInput("Пользователь был не найден");
            }
            Review answer =  reviewHib.getByUserAndGood(userId, good);
            if (answer == null){
                throw new DoesNoeExist("Does not exist");
            }
            return mapper.toDto(answer);

        }catch(NotCorrectInput | DoesNoeExist e){
            throw e;
        }catch (Exception e){
            logger.error("ReviewForUserService getByUserAndGood:" + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public ReviewDto createReview(ReviewCreateDto dto, String login, Long goodId){
        try{
            User user = userHib.getByLoginSmallVersion(login);
            if (user == null){
                throw new NotCorrectInput("Пользователь не был найден");
            }
            Good good = goodHib.findById(goodId, logger);
            if (good == null){
                throw new NotCorrectInput("Такой продукт не найден");
            }

            if (good.getModeratorStatus() == GoodStatusFromModerator.SUSPICIOUS){
                throw new PermissionDenied("Currently good is unavailable for reviews");
            }

            return mapper.toDto(reviewHib.createReview(dto, good, user));
        }catch (PermissionDenied | NotCorrectInput e){
            throw e;
        }
        catch (Exception e) {
            logger.error("ReviewForUserService createReview:" + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public void deleteReview(Long goodId, String username){
        try{
            Long userId = userHib.getUserIdByLogin(username);
            if (userId == null){
                throw new NotCorrectInput("Пользователь не был найден");
            }

            reviewHib.deleteReview(goodId, userId);
        }catch(NotCorrectInput e){
            throw e;
        }catch (Exception e){
            logger.error("ReviewForUserService deleteReview:" + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<ReviewDto> getByFilters(ReviewForUserFilters filters){
        try{
            List<Review> reviews = reviewHib.getMinByFilters(filters);
            if (reviews.isEmpty()){
                return List.of();
            }
            return listToDto(reviews);
        }catch (Exception e){
            logger.error("ReviewForUserService getByFilters:" + e.getMessage());
            throw e;
        }

    }

    private List<ReviewDto> listToDto(List<Review> reviews){
        try{
            List<ReviewDto> dtos = new ArrayList<ReviewDto>();
            for (Review review : reviews){
                dtos.add(mapper.toDto(review));
            }
            return dtos;
        } catch (Exception e){
            logger.error("ReviewForUserService listToDto:" + e.getMessage());
            throw e;
        }

    }



}
