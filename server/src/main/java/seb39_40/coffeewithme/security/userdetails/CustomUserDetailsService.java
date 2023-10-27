package seb39_40.coffeewithme.security.userdetails;


import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import seb39_40.coffeewithme.user.domain.User;
import seb39_40.coffeewithme.user.domain.UserStatus;
import seb39_40.coffeewithme.user.repository.UserRepository;

import javax.security.auth.login.AccountException;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userEntity = userRepository.findByEmail(email);
        if(!userEntity.isPresent())
            throw new UsernameNotFoundException("존재하지 않는 이메일입니다.");
        if(!userEntity.get().getStatus().equals(UserStatus.USER_LOGIN))
            throw new JwtException( "올바른 토큰이 아닙니다. 로그인 상태가 아닙니다.");
        return new CustomUserDetails(userEntity.get());
    }

    public boolean loginUser(User user){
        if(user.getStatus().equals(UserStatus.USER_LOGIN)) return false;
        userRepository.updateUserStatus(user.getEmail(),UserStatus.USER_LOGIN);
        return true;
    }

    public void logoutUser(String email){
        userRepository.updateUserStatus(email,UserStatus.USER_LOGOUT);
    }
}
