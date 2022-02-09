package com.nimvb.app.discord.security.entrypoint;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Entrypoint or the state which the exchange should be configured for when there is an exception
 * during authenticating the provided {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}
 */
public class UsernamePasswordTokenServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        if(exchange == null){
            return Mono.empty();
        }
        return Mono.fromCallable(() -> Mono.just(exchange.getResponse())).flatMap(serverHttpResponseMono -> serverHttpResponseMono)
                .flatMap(serverHttpResponse -> {
                    serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
                    serverHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return serverHttpResponse.setComplete();
                });
    }
}
