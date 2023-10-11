package seb39_40.coffeewithme.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import seb39_40.coffeewithme.image.dto.ImageResponseDto;
import seb39_40.coffeewithme.user.domain.User;

@Getter
@AllArgsConstructor
public class UserInfoResponseDto{
    private String userName;
    private String email;
    private String mobile;
    private ImageResponseDto profilePhoto;

    public static UserInfoResponseDto from(User user){
        ImageResponseDto imageDto=new ImageResponseDto();
        imageDto.setId(user.getProfilePhoto().getId());
        imageDto.setPath(user.getProfilePhoto().getName());
        return new UserInfoResponseDto(user.getUserName(), user.getEmail(), user.getMobile(), imageDto);
    }
}
