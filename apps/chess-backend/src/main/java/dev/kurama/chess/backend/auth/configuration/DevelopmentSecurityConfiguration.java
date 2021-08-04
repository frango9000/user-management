package dev.kurama.chess.backend.auth.configuration;

import static dev.kurama.chess.backend.auth.constant.SecurityConstant.DEVELOPMENT_PUBLIC_URLS;

import dev.kurama.chess.backend.auth.filter.JWTAccessDeniedHandler;
import dev.kurama.chess.backend.auth.filter.JWTAuthenticationEntryPoint;
import dev.kurama.chess.backend.auth.filter.JWTAuthorizationFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Profile({"development"})
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class DevelopmentSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final JWTAuthorizationFilter jwtAuthorizationFilter;
  private final JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JWTAccessDeniedHandler jwtAccessDeniedHandler;
  private final UserDetailsService userDetailsService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public DevelopmentSecurityConfiguration(JWTAuthorizationFilter jwtAuthorizationFilter,
    JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint,
    JWTAccessDeniedHandler jwtAccessDeniedHandler,
    @Qualifier("userDetailsService") UserDetailsService userDetailsService,
    BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    this.userDetailsService = userDetailsService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .csrf().disable()
      .cors().and()
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
      .authorizeRequests().antMatchers(DEVELOPMENT_PUBLIC_URLS).permitAll()
      .anyRequest().authenticated().and()
      .exceptionHandling().accessDeniedHandler(jwtAccessDeniedHandler)
      .authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
      .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

}