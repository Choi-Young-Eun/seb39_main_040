package seb39_40.coffeewithme.security.jwt;

import lombok.Getter;

public enum TokenType {
    ACCESS("Access Token"),
    REFRESH("Refresh Token");

    @Getter
    private String type;

    TokenType(String type) {
        this.type = type;
    }
}
