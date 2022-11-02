package com.runsidekick.api.configuration;

import com.runsidekick.api.security.AuthenticationFilter;
import com.runsidekick.api.service.ApiAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.Filter;

/**
 * @author yasin.kalafat
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile({"!test"})
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final ApiAuthService apiAuthService;
    @Bean
    public FilterRegistrationBean authenticationFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        Filter authenticationFilter = new AuthenticationFilter(apiAuthService);
        registration.setFilter(authenticationFilter);
        registration.addUrlPatterns("/api/v1/*");
        registration.setOrder(1);
        return registration;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/swagger/**").permitAll()
                .antMatchers("/ping").permitAll()
                .and()
                .cors()
                .configurationSource(request -> {
                    CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
                    corsConfiguration.addAllowedMethod("*");
                    return corsConfiguration;
                });
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().
                antMatchers("/swagger/**").
                antMatchers("/ping");
    }
}