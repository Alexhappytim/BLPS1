package com.blps.app.security;

import com.blps.app.security.jaas.AppJaasConfiguration;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.jaas.AbstractJaasAuthenticationProvider;
import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public javax.security.auth.login.Configuration jaasConfiguration() {
        return new AppJaasConfiguration();
    }

    @Bean
    public AbstractJaasAuthenticationProvider jaasAuthenticationProvider(javax.security.auth.login.Configuration jaasConfiguration,
                                                                         AuthorityGranter rolePrincipalAuthorityGranter) {
        SpringJaasAuthenticationProvider provider = new SpringJaasAuthenticationProvider(jaasConfiguration);
        provider.setLoginContextName("blps");
        provider.setAuthorityGranters(new AuthorityGranter[] { rolePrincipalAuthorityGranter });
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider jaasAuthenticationProvider) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
                        })
                )
                .authenticationProvider(jaasAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/confirm-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/admin/register").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/submissions/review").hasAnyRole("MENTOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/courses", "/api/blocks", "/api/tasks").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/courses/**", "/api/blocks/**", "/api/tasks/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**", "/api/blocks/**", "/api/tasks/**").hasRole("ADMIN")
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
