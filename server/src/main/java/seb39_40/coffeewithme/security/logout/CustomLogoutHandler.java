package seb39_40.coffeewithme.security.logout;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import seb39_40.coffeewithme.security.jwt.JwtProvider;
import seb39_40.coffeewithme.security.userdetails.CustomUserDetailsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String email = authentication.getPrincipal().toString();
        String token = authentication.getCredentials().toString();

        Claims claims = jwtProvider.parseToken(token);

        jwtProvider.removeRefreshToken("Refresh:"+email);
        jwtProvider.saveToken("BAN:"+token,email, jwtProvider.getExpirationToClaims(claims));
        userDetailsService.logoutUser(email);
    }
}
