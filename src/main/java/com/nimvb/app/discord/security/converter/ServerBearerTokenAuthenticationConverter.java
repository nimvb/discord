package com.nimvb.app.discord.security.converter;

import com.nimvb.app.discord.security.resource.BearerTokenAuthenticationToken;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerBearerTokenAuthenticationConverter implements ServerAuthenticationConverter {

    private static final Pattern authorizationPattern = Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
            Pattern.CASE_INSENSITIVE);


    @Setter
    private String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;


    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {

        return resolveFromAuthorizationHeader(exchange.getRequest().getHeaders())
                .map(BearerTokenAuthenticationToken::new);
    }



    private Mono<String> resolveFromAuthorizationHeader(HttpHeaders headers) {
        if(headers == null){
            return Mono.empty();
        }
        String authorization = headers.getFirst(this.bearerTokenHeaderName);
        if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
            return Mono.empty();
        }
        Matcher matcher = authorizationPattern.matcher(authorization);
        if (!matcher.matches()) {
            return Mono.empty();
        }
        return Mono.fromSupplier(() -> matcher.group("token"));
    }
}
