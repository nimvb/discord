package com.nimvb.app.discord.configuration;

import com.nimvb.app.discord.security.converter.ServerBearerTokenAuthenticationConverter;
import com.nimvb.app.discord.security.converter.ServerUsernamePasswordAuthenticationConverter;
import com.nimvb.app.discord.security.entrypoint.UsernamePasswordTokenServerAuthenticationEntryPoint;
import com.nimvb.app.discord.security.handler.AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.util.matcher.*;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.Objects;

@EnableWebFluxSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityOfPrivateResourcesConfiguration {


    private final ReactiveAuthenticationManager reactiveAuthenticationManager;


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity security) {


        return security
                .csrf().disable()
                .logout().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
//                .pathMatchers(HttpMethod.POST,"/api/v1/authenticate").permitAll()
                .anyExchange().authenticated()
                .and()
                .addFilterAt(constructBearerTokenValidationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAfter(constructLoginFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private WebFilter constructBearerTokenValidationFilter() {
        AuthenticationWebFilter bearerFilter = new AuthenticationWebFilter(reactiveAuthenticationManager);
        bearerFilter.setRequiresAuthenticationMatcher(new NegatedServerWebExchangeMatcher(new PathPatternParserServerWebExchangeMatcher("/api/v1/authenticate**",HttpMethod.POST)));
        bearerFilter.setServerAuthenticationConverter(new ServerBearerTokenAuthenticationConverter());
        bearerFilter.setAuthenticationSuccessHandler(new AuthenticationSuccessHandler());
        return bearerFilter;
    }

    private WebFilter constructLoginFilter() {
        AuthenticationWebFilter loginFilter = new AuthenticationWebFilter(reactiveAuthenticationManager);
        loginFilter.setServerAuthenticationConverter(new ServerUsernamePasswordAuthenticationConverter());
        loginFilter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(new UsernamePasswordTokenServerAuthenticationEntryPoint()));
        loginFilter.setRequiresAuthenticationMatcher(
                new AndServerWebExchangeMatcher(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/api/v1/authenticate"),
                        ServerWebExchangeMatchers.matchers(exchange ->
                                Mono.fromCallable(() -> MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(exchange.getRequest().getHeaders().getContentType())
                                        ).flatMap(isContentTypeMatched -> isContentTypeMatched ?
                                                ServerWebExchangeMatcher.MatchResult.match() :
                                                ServerWebExchangeMatcher.MatchResult.notMatch()))));
        return loginFilter;
    }
}
