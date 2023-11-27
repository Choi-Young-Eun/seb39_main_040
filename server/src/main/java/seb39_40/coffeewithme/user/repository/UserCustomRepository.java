package seb39_40.coffeewithme.user.repository;

import seb39_40.coffeewithme.user.domain.UserStatus;

public interface UserCustomRepository {
    void updateUserStatus(String email, UserStatus status);
}
