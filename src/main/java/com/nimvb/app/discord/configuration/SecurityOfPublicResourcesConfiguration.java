package com.nimvb.app.discord.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

@EnableWebFluxSecurity
@Configuration
public class SecurityOfPublicResourcesConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityWebFilterChain chain(ServerHttpSecurity security){
        return security
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .logout().disable()
                .anonymous().disable()
                .securityMatcher(new OrServerWebExchangeMatcher(
                        new PathPatternParserServerWebExchangeMatcher("/api/v1/users/**",HttpMethod.POST),
                        new PathPatternParserServerWebExchangeMatcher("/api/v1/users/**",HttpMethod.OPTIONS),
                        new PathPatternParserServerWebExchangeMatcher("/v3/api-docs/**"),
                        new PathPatternParserServerWebExchangeMatcher("/swagger-ui/**"),
                        new PathPatternParserServerWebExchangeMatcher("/swagger-ui.html"),
                        new PathPatternParserServerWebExchangeMatcher("/webjars/swagger-ui/**")
                ))
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/swagger-ui/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/users/**").permitAll()
                .anyExchange().authenticated()
                .and()
                .build();
    }
}
