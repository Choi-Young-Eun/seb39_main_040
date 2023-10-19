package seb39_40.coffeewithme.security.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import seb39_40.coffeewithme.security.userdetails.CustomUserDetails;
import seb39_40.coffeewithme.security.userdetails.CustomUserDetailsService;
import seb39_40.coffeewithme.user.domain.UserStatus;

@RequiredArgsConstructor
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        CustomUserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails.getUser().getStatus().equals(UserStatus.USER_WITHDRAW)) {
            throw new DisabledException("Provider - authenticate() : 탈퇴한 회원입니다.");
        } else if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Provider - authenticate() : 비밀번호가 일치하지 않습니다.");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
