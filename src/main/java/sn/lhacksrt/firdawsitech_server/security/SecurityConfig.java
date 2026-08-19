package sn.lhacksrt.firdawsitech_server.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthFilter jwtFilter;

  @Value("${app.cors.allowed-origins:https://firdawsitech.sn,https://www.firdawsitech.sn}")
  private String allowedOrigins;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .cors(cors -> cors.configurationSource(corsSource()))
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(reg -> reg
        // Auth public
        .requestMatchers("/auth/**").permitAll()
              .requestMatchers("/files/images/**").permitAll()
        // Ex: vitrine publique si tu en as
        .requestMatchers(HttpMethod.GET, "/vitrine/**").permitAll()
              .requestMatchers(HttpMethod.GET, "/shop/**").permitAll()
              .requestMatchers(HttpMethod.POST, "/shop/**").permitAll()
        // Tout le reste protégé
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
    return cfg.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  private CorsConfigurationSource corsSource() {
      final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      final CorsConfiguration config = new CorsConfiguration();
      config.setAllowCredentials(true);
      config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .collect(Collectors.toList()));
      config.setAllowedHeaders(Collections.singletonList("*"));
      config.setAllowedMethods(Arrays.stream(HttpMethod.values()).map(HttpMethod::name).collect(Collectors.toList()));
      source.registerCorsConfiguration("/**", config);
      return source;
  }
}
