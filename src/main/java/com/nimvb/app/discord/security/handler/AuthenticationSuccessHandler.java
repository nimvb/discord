package com.nimvb.app.discord.security.handler;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

public class AuthenticationSuccessHandler extends WebFilterChainServerAuthenticationSuccessHandler {
    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {

        if(authentication == null){
            return Mono.error(new BadCredentialsException("invalid token"));
        }
        return super.onAuthenticationSuccess(webFilterExchange, authentication)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}
