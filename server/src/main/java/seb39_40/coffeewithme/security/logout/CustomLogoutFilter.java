package seb39_40.coffeewithme.security.logout;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import seb39_40.coffeewithme.exception.ErrorResponse;
import seb39_40.coffeewithme.exception.ExceptionCode;
import seb39_40.coffeewithme.security.jwt.JwtProvider;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class CustomLogoutFilter extends LogoutFilter {
    private LogoutSuccessHandler logoutSuccessHandler;
    private LogoutHandler logoutHandler;
    private final JwtProvider jwtProvider;

    public CustomLogoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler handlers, JwtProvider jwtProvider) {
        super(logoutSuccessHandler, handlers);
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.logoutHandler = handlers;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if(requiresLogout(httpRequest, httpResponse)){
            try {
                String jwt = jwtProvider.substringToken(httpRequest.getHeader("Token"));
                Claims claims = jwtProvider.parseToken(jwt);
                String email = jwtProvider.getEmailToClaims(claims);

                Authentication authentication = new UsernamePasswordAuthenticationToken(email, jwt);
                this.logoutHandler.logout(httpRequest, httpResponse, authentication);
                this.logoutSuccessHandler.onLogoutSuccess(httpRequest, httpResponse, authentication);
            }catch(ExpiredJwtException e){
                setExceptionResponse(httpResponse, ExceptionCode.TOKEN_ACCESS_EXPIRATION);
            }catch(JwtException e){
                    setExceptionResponse(httpResponse, ExceptionCode.TOKEN_PRECONDITION_FAILED);
            }
        }else {
            chain.doFilter(request, response);
        }
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
