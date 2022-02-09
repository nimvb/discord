package com.nimvb.app.discord.security.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
class AuthenticationSuccessHandlerTest {


    @Mock
    private WebFilterChain chain;

    private WebFilterExchange webFilterExchange;


    @BeforeEach
    void init() {
        MockServerHttpRequest request  = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        Mockito.when(chain.filter(exchange)).thenReturn(Mono.empty());
        webFilterExchange = new WebFilterExchange(exchange, chain);
    }

    @Test
    void ShouldReturnMonoWithErrorWhenNULLAuthenticationIsPassed() {

        Mono<Void> result = new AuthenticationSuccessHandler().onAuthenticationSuccess(webFilterExchange, null);
        StepVerifier.create(result).verifyError();
        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();
        StepVerifier.create(result).expectErrorMessage("invalid token").verify();
    }

    @Test
    void ShouldSetReactiveSecurityContextHolderWhenValidAuthenticationIsPassed() {

        UsernamePasswordAuthenticationToken token  = new UsernamePasswordAuthenticationToken("username", "password", Collections.emptyList());
        Mono<Void>                          result = new AuthenticationSuccessHandler().onAuthenticationSuccess(webFilterExchange, token);

        StepVerifier.create(result).expectNextCount(0).verifyComplete();
        StepVerifier.create(result).expectAccessibleContext().matches(context -> {
            if(context.hasKey(SecurityContext.class)){
                Mono<SecurityContext> securityContextMono =
                        (Mono<SecurityContext>) context.stream()
                        .filter(objectObjectEntry -> objectObjectEntry.getKey().equals(SecurityContext.class))
                        .findAny()
                        .get()
                        .getValue();
                Mono<Authentication> authenticationMono = securityContextMono.map(SecurityContext::getAuthentication);
                Authentication       authentication              = authenticationMono.block();
                return authentication != null && authentication.equals(token);
            }
            return false;
        }).then().verifyComplete();
    }

}