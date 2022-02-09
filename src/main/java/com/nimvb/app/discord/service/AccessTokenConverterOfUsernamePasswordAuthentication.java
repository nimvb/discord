package com.nimvb.app.discord.service;

import com.nimvb.app.discord.model.AccessToken;
import com.nimvb.app.discord.security.service.SecretProvider;
import com.nimvb.app.discord.security.util.Jwe;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessTokenConverterOfUsernamePasswordAuthentication implements AccessTokenConverter<UsernamePasswordAuthenticationToken> {

    private final SecretProvider secretProvider;
    private final Clock clock;

    @Override
    public Mono<AccessToken> convert(@NonNull UsernamePasswordAuthenticationToken authentication) {
        Instant now = Instant.now(clock);
        Date expiredAt = Date.from(now.plus(1, ChronoUnit.HOURS));
        Date refreshTokenExpiredAt = Date.from(now.plus(24, ChronoUnit.HOURS));
        return Mono.just(authentication)
                .zipWith(Mono.fromCallable(() -> Jwe.Algorithm.AES128HS256(secretProvider.secret())))
                .flatMap(tuple -> {
                    final UsernamePasswordAuthenticationToken authenticationToken = tuple.getT1();
                    final Jwe.Algorithm algorithm = tuple.getT2();
                    final Jwe.Builder accessTokenBuilder = Jwe
                            .create()
                            .withSubject(authentication.getName())
                            .withExpiredAt(expiredAt)
                            .withIssuer("users")
                            .withClaim("roles", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
                    final Jwe.Builder refreshTokenBuilder = Jwe
                            .create()
                            .withSubject(authentication.getName())
                            .withExpiredAt(refreshTokenExpiredAt)
                            .withIssuer("users")
                            .withClaim("roles", Collections.emptyList());
                    return Mono.fromCallable(() -> {
                        final String accessToken = accessTokenBuilder.encrypt(algorithm);
                        final String refreshToken = refreshTokenBuilder.encrypt(algorithm);
                        return new AccessToken(accessToken, "Bearer", expiredAt.toInstant().toEpochMilli(), refreshToken, "");
                    });

                });
    }


}
