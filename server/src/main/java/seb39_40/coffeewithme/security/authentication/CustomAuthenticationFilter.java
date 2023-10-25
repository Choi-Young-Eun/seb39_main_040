package seb39_40.coffeewithme.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import seb39_40.coffeewithme.exception.ErrorResponse;
import seb39_40.coffeewithme.exception.ExceptionCode;
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
    private final String TYPE="Bearer ";
    private final long REFRESH_EXPIRATION= 1000 * 60 * 60;

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
        String at = jwtProvider.createToken("Access Token",user.getUsername());
        String rt = jwtProvider.createToken("Refresh Token",user.getUsername());

        jwtProvider.saveToken("Refresh:"+user.getUsername(),rt, REFRESH_EXPIRATION);
        response.setHeader("AccessToken",TYPE+at);
        response.setHeader("RefreshToken",TYPE+rt);
        response.setStatus(setSuccessResponse(user.getUser()));
        log.info("** Success Login [{}]",user.getUsername());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        if(exception instanceof BadCredentialsException){
            setExceptionResponse(response,ExceptionCode.USER_UNAUTHORIZED);
        }
        else if(exception instanceof LockedException){
            setExceptionResponse(response,ExceptionCode.USER_FORBIDDEN);
        }
        else if(exception instanceof UsernameNotFoundException){
            setExceptionResponse(response,ExceptionCode.USER_NOT_FOUND);
        }
    }

    private int setSuccessResponse(User user){
        if(userDetailsService.loginUser(user))
            return HttpStatus.OK.value();
        else
            return HttpStatus.CREATED.value();
    }

    private void setExceptionResponse(HttpServletResponse response, ExceptionCode exceptionCode) throws IOException {
        response.setStatus(exceptionCode.getStatus());
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        ErrorResponse error = ErrorResponse.of(exceptionCode);
        new ObjectMapper().writeValue(response.getWriter(), error);
        log.error("** Exception - Code : {}, Message : {}", exceptionCode.getStatus(), exceptionCode.getMessage());
    }
}
