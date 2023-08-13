package ru.mhelper.cfg;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.mhelper.controllers.AuthController;
import ru.mhelper.controllers.ChromeExtensionController;
import ru.mhelper.controllers.SimpleController;
import ru.mhelper.models.users.ERole;
import ru.mhelper.security.jwt.JwtTokenFilter;
import ru.mhelper.security.jwt.JwtTokenProvider;

import static ru.mhelper.controllers.AuthController.CODE_URL;
import static ru.mhelper.controllers.AuthController.TEST_JWT;

@Profile("!test")
@Configuration
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String CHROME_API = ChromeExtensionController.URL + "/***";

    public static final String CHROME_AUTH = AuthController.URL + "/";

    public static final String TEST_CHROME_JWT_AUTH = AuthController.URL + TEST_JWT + "/";

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers(
                                        "/v1/auth/code/**",
                                        CHROME_AUTH,
                                        AuthController.URL + CODE_URL + "***",
                                        SimpleController.INDEX_PAGE_NAME
                                ).permitAll()

                                .requestMatchers("/api/v1/management/**").hasAnyRole(ERole.ROLE_ADMIN.getName(), ERole.CHROME_EXTENSION.getName())
                                .requestMatchers("/v1/auth/test/**").hasAuthority(ERole.CHROME_EXTENSION.getName())
                                .anyRequest()
                                .authenticated())
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
