package seb39_40.coffeewithme.user.domain;

import lombok.Getter;

public enum UserStatus {
    USER_SIGNUP("signup"),
    USER_WITHDRAW("withdraw");

    @Getter
    private String status;

    UserStatus(String status) {
        this.status = status;
    }
}
