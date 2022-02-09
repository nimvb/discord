package com.nimvb.app.discord.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeyLengthException;
import com.nimvb.app.discord.model.AccessToken;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.function.Function;

public interface AccessTokenConverter<T extends Authentication> {

    /**
     * Converts an {@link Authentication} to the {@link AccessToken}
     * @param authentication
     * @return Mono provides the produced {@link AccessToken}, otherwise {@link Mono} wth error
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws JOSEException
     */
    Mono<AccessToken> convert(@NonNull T authentication);
}
