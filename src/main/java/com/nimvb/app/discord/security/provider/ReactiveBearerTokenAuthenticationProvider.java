package com.nimvb.app.discord.security.provider;

import com.nimvb.app.discord.security.resource.BearerTokenAuthenticationToken;
import com.nimvb.app.discord.security.service.SecretProvider;
import com.nimvb.app.discord.security.util.Jwe;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReactiveBearerTokenAuthenticationProvider implements ReactiveAuthenticationProvider {


    @NonNull private final SecretProvider secretProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return
                Mono.fromSupplier(() -> Mono.just(authentication).cast(BearerTokenAuthenticationToken.class))
                        .flatMap(bearerTokenAuthenticationTokenMono -> bearerTokenAuthenticationTokenMono)
                        .flatMap(token -> Mono.fromCallable(() -> Jwe
                                .require(Jwe.Algorithm.AES128HS256(secretProvider.secret()))
                                .build()
                                .decrypt(token.getToken())))
                        .flatMap(claimsSet -> {
                            boolean expired = Instant.now().isAfter(claimsSet.getExpirationTime().toInstant());
                            if (expired) {
                                return Mono.error(new CredentialsExpiredException("invalid token"));
                            }
                            return Mono
                                    .deferContextual(contextView -> {
                                        BearerTokenAuthenticationToken token = contextView.get("token");
                                        Mono<Authentication> authenticationToken = Mono.fromCallable(() -> {
                                            return new BearerTokenAuthenticationToken(token.getToken(),
                                                    claimsSet.getSubject(),
                                                    Optional.ofNullable(claimsSet.getStringListClaim("roles"))
                                                            .orElseGet(Collections::emptyList)
                                                            .stream()
                                                            .map(SimpleGrantedAuthority::new)
                                                            .collect(Collectors.toUnmodifiableList()));
                                        });
                                        return authenticationToken;
                                    })
                                    .contextWrite(context -> context.put("token", authentication));
                        })
                        .onErrorResume(throwable -> Mono.empty())
                        .switchIfEmpty(Mono.error(new BadCredentialsException("invalid token")));

    }

    @Override
    public Mono<Boolean> supports(Class<?> authentication) {
        return Mono.just(false)
                .flatMap(supports -> {
                    if(authentication == null){
                        return Mono.just(false);
                    }
                    return Mono.just(BearerTokenAuthenticationToken.class.isAssignableFrom(authentication));
                });
    }
}
