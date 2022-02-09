package com.nimvb.app.discord.security.converter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ServerUsernamePasswordAuthenticationConverter implements ServerAuthenticationConverter {

    public final static String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
    public final static String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        if(exchange == null){
            return Mono.empty();
        }
        return exchange
                .getFormData()
                .onErrorResume(throwable -> Mono.empty())
                .map(stringStringMultiValueMap -> {
                    String username = stringStringMultiValueMap.getFirst(SPRING_SECURITY_FORM_USERNAME_KEY);
                    String password = stringStringMultiValueMap.getFirst(SPRING_SECURITY_FORM_PASSWORD_KEY);
                    username = username != null ? username.trim() : "";
                    password = password != null ? password.trim() : "";
                    return new UsernamePasswordAuthenticationToken(username, password);
                });

    }
}
