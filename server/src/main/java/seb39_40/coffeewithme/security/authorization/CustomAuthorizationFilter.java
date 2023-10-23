package seb39_40.coffeewithme.security.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import seb39_40.coffeewithme.exception.ErrorResponse;
import seb39_40.coffeewithme.exception.ExceptionCode;
import seb39_40.coffeewithme.security.jwt.JwtProvider;
import seb39_40.coffeewithme.security.userdetails.CustomUserDetails;
import seb39_40.coffeewithme.security.userdetails.CustomUserDetailsService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService detailsService;
    private String TYPE="Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        String token = request.getHeader("Token");

        if(!request.getRequestURI().startsWith("/api")){
            filterChain.doFilter(request, response);
        }else{
            try{
                String jwt = jwtProvider.substringToken(token);
                Claims claims = jwtProvider.parseToken(jwt);
                String email = jwtProvider.getEmailToClaims(claims);

                CustomUserDetails user = detailsService.loadUserByUsername(email);

                if(path.equals("/users/token")){
                    jwtProvider.validationTheSameToken(email, jwt);
                    String new_at = jwtProvider.createAccessToken(user);
                    String new_rt = jwtProvider.createRefreshToken(user.getUsername());

                    jwtProvider.saveRefreshToken(email, new_rt);
                    response.setHeader("AccessToken", TYPE + new_at);
                    response.setHeader("RefreshToken", TYPE + new_rt);
                }
                Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            }catch(ExpiredJwtException e){
                if(path.equals("/api/users/token")){
                    detailsService.logoutUser2(e.getClaims().get("email").toString());
                    setExceptionResponse(response, ExceptionCode.TOKEN_REFRESH_EXPIRATION);
                }else{
                    setExceptionResponse(response, ExceptionCode.TOKEN_ACCESS_EXPIRATION);
                }
            }catch(NullPointerException e){
                setExceptionResponse(response, ExceptionCode.TOKEN_BAD_REQUEST);
            }catch(JwtException e){
                setExceptionResponse(response, ExceptionCode.TOKEN_PRECONDITION_FAILED);
            }
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

