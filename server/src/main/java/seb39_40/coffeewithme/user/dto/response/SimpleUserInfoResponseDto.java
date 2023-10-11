package seb39_40.coffeewithme.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import seb39_40.coffeewithme.user.domain.User;

@Getter
@AllArgsConstructor
public class SimpleUserInfoResponseDto{
    Long id;
    String name;

    public static SimpleUserInfoResponseDto from(User user){
        return new SimpleUserInfoResponseDto(user.getId(), user.getUserName());
    }
}
