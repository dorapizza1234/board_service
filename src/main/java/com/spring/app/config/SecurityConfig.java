package com.spring.app.config;

import com.spring.app.security.JwtAuthenticationFilter;
import com.spring.app.security.JwtTokenProvider;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, e) -> response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Bean
    AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, e) -> response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        String[] publicUris = {
            "/notice/list", "/notice/view", "/notice/faq",
            "/inquiry/list", "/inquiry/write", "/inquiry/admin/api",
            "/index", "/favicon.ico"
        };

        http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
            .requestMatchers(publicUris).permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().hasAnyRole("USER", "ADMIN")
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
        .httpBasic(h -> h.disable())
        .exceptionHandling(e -> e
            .authenticationEntryPoint(customAuthenticationEntryPoint())
            .accessDeniedHandler(customAccessDeniedHandler())
        )
        .headers(h -> h.frameOptions(f -> f.sameOrigin()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .logout(logout -> logout
            .logoutUrl("/auth/logout")
            .addLogoutHandler((req, res, auth) -> {
                HttpSession session = req.getSession();
                if (session != null) session.invalidate();
            })
            .logoutSuccessUrl("/index")
            .deleteCookies("refreshToken")
        );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
            .requestMatchers("/bootstrap-4.6.2-dist/**", "/css/**", "/images/**", "/js/**");
    }
}
