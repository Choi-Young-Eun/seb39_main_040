package seb39_40.coffeewithme.review.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;
import seb39_40.coffeewithme.user.dto.response.SimpleUserInfoResponseDto;

@Data @NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReviewResponseDto {
    @Data @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ReviewInfo{
        String[] tags;
        SimpleUserInfoResponseDto user;
        Long id;
        String description;
        Integer score;
        String reviewImg;
    }

    @Data @NoArgsConstructor
    public static class ImageInfo{
        Long id;
        String path;
    }

}
