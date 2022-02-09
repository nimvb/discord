package com.nimvb.app.discord.security.provider;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import reactor.core.publisher.Mono;

public interface ReactiveAuthenticationProvider {


    /**
     * Authenticate the provided {@code authentication} token
     * @param authentication the token to be authenticated
     * @return {@link Mono} that provides the authenticated token, otherwise an error of type {@link AuthenticationException}
     * will be generated
     * @throws AuthenticationException thrown when authentication failed
     */
    Mono<Authentication> authenticate(Authentication authentication);

    /**
     * Checks the provider supports authenticating the token that has type of {@code authenticate}
     * @param authentication type of token
     * @return Mono that it's next value will be true if the provider supports the type, otherwise false
     */
    Mono<Boolean> supports(Class<?> authentication);

}
