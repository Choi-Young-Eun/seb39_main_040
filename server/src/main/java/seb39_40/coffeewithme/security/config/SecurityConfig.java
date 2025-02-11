package seb39_40.coffeewithme.security.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;
import seb39_40.coffeewithme.security.authorization.*;
import seb39_40.coffeewithme.security.authentication.CustomAuthenticationFilter;
import seb39_40.coffeewithme.security.jwt.JwtProvider;
import seb39_40.coffeewithme.security.logout.CustomLogoutFilter;
import seb39_40.coffeewithme.security.logout.CustomLogoutHandler;
import seb39_40.coffeewithme.security.logout.CustomLogoutSuccessHandler;
import seb39_40.coffeewithme.security.userdetails.CustomUserDetailsService;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {
    private final CorsFilter corsFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProvider jwtProvider;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomLogoutHandler customLogoutHandler;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.headers().frameOptions().disable();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable()
                .logout().disable()
                .httpBasic().disable()
                .authorizeRequests()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/api/users/**").hasRole("USER") //로그인 한 사용자만 사용할 수 잇도록
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
                .and()
                .addFilter(corsFilter)
                .addFilter(customAuthenticationFilter())
                .addFilter(customLogoutFilter())
                .addFilterBefore(customAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .accessDeniedHandler(customAccessDeniedHandler);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter =
                new CustomAuthenticationFilter(authenticationManager(authenticationConfiguration),jwtProvider,
                        customUserDetailsService);
        customAuthenticationFilter.setFilterProcessesUrl("/users/login");
        return customAuthenticationFilter;
    }

    @Bean
    public CustomLogoutFilter customLogoutFilter(){
        CustomLogoutFilter customLogoutFilter =
                new CustomLogoutFilter(new CustomLogoutSuccessHandler(), customLogoutHandler, jwtProvider);
        customLogoutFilter.setFilterProcessesUrl("/users/logout");
        return customLogoutFilter;
    }

    @Bean
    public CustomAuthorizationFilter customAuthorizationFilter() {
        CustomAuthorizationFilter customAuthorizationFilter =
                new CustomAuthorizationFilter(jwtProvider,customUserDetailsService);
        return customAuthorizationFilter;
    }

}
