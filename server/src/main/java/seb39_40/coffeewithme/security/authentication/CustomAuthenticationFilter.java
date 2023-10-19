package seb39_40.coffeewithme.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import seb39_40.coffeewithme.exception.ErrorResponse;
import seb39_40.coffeewithme.security.jwt.JwtProvider;
import seb39_40.coffeewithme.security.userdetails.CustomUserDetails;
import seb39_40.coffeewithme.security.userdetails.CustomUserDetailsService;
import seb39_40.coffeewithme.user.domain.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private String TYPE="Bearer ";

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider,
                                      CustomUserDetailsService userDetailsService) {
        super.setAuthenticationManager(authenticationManager);
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try{
            ObjectMapper om = new ObjectMapper();
            User user = om.readValue(request.getInputStream(), User.class);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
            Authentication authResult = this.getAuthenticationManager().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authResult);
            return authResult;
        } catch(IOException e) {
            e.printStackTrace();;
        }
        return null;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        CustomUserDetails user = (CustomUserDetails) authResult.getPrincipal();
        String at = jwtProvider.createAccessToken(user);
        String rt = jwtProvider.createRefreshToken(user.getUsername());

        jwtProvider.saveRefreshToken(user.getUsername(),rt);
        response.setHeader("AccessToken",TYPE+at);
        response.setHeader("RefreshToken",TYPE+rt);
        response.setStatus(setSuccessResponse(user.getUser()));
        log.info("** Success Login [{}]",user.getUsername());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if(exception instanceof BadCredentialsException){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("utf-8");
            ErrorResponse e = ErrorResponse.of(HttpStatus.UNAUTHORIZED,"Email 혹은 Password가 일치하지 않습니다.");
            log.error("** BadCredentialsException in Login : Email 혹은 Password가 일치하지 않습니다.");
            new ObjectMapper().writeValue(response.getWriter(), e);
        }
        else if(exception instanceof DisabledException){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("utf-8");
            ErrorResponse e = ErrorResponse.of(HttpStatus.BAD_REQUEST,"존재하지 않는 회원입니다.");
            log.error("** DisabledException in Login : 존재하지 않는 회원입니다.");
            new ObjectMapper().writeValue(response.getWriter(), e);
        }
        else if(exception instanceof UsernameNotFoundException){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("utf-8");
            ErrorResponse e = ErrorResponse.of(HttpStatus.BAD_REQUEST,"존재하지 않는 회원입니다.");
            log.error("** UsernameNotFoundException in Login : 존재하지 않는 회원입니다.");
            new ObjectMapper().writeValue(response.getWriter(), e);
        }
    }

    private int setSuccessResponse(User user){
        if(userDetailsService.loginUser(user))
            return HttpStatus.OK.value();
        else
            return HttpStatus.CREATED.value();
    }
}
