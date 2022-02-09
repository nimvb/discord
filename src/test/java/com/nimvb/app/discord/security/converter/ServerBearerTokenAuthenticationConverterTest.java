package com.nimvb.app.discord.security.converter;

import com.nimvb.app.discord.security.resource.BearerTokenAuthenticationToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

@ExtendWith(SpringExtension.class)
class ServerBearerTokenAuthenticationConverterTest {

    private final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private final String BEARER_TOKEN = "BEARER " + TOKEN;

    private final MockServerWebExchange mockServerWebExchangeWithValidBearerToken = new MockServerWebExchange
            .Builder(MockServerHttpRequest
            .get("/")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            .build())
            .build();

    private final MockServerWebExchange mockServerWebExchangeWithInvalidBearerToken = new MockServerWebExchange
            .Builder(MockServerHttpRequest
            .get("/")
            .header(HttpHeaders.AUTHORIZATION, TOKEN)
            .build())
            .build();

    private final MockServerWebExchange mockServerWebExchangeWithNoHeaders = new MockServerWebExchange
            .Builder(MockServerHttpRequest.method(HttpMethod.GET,"/").build())
            .build();


    @Test
    public void ShouldReturnEmptyMonoWhenNoHeadersIsProvided(){
        ServerBearerTokenAuthenticationConverter converter = new ServerBearerTokenAuthenticationConverter();
        final Mono<Authentication> authenticationMono = converter.convert(mockServerWebExchangeWithNoHeaders);
        StepVerifier.create(authenticationMono).expectNextCount(0).verifyComplete();
    }

    @Test
    public void ShouldReturnEmptyMonoWhenInvalidBearerTokenIsProvided(){
        ServerBearerTokenAuthenticationConverter converter = new ServerBearerTokenAuthenticationConverter();
        final Mono<Authentication> authenticationMono = converter.convert(mockServerWebExchangeWithInvalidBearerToken);
        StepVerifier.create(authenticationMono).expectNextCount(0).verifyComplete();
    }

    @Test
    public void ShouldReturnMonoWithAuthenticationWhenValidBearerTokenIsProvided(){
        ServerBearerTokenAuthenticationConverter converter = new ServerBearerTokenAuthenticationConverter();
        final Mono<Authentication> authenticationMono = converter.convert(mockServerWebExchangeWithValidBearerToken);
        StepVerifier.create(authenticationMono).expectNextCount(1).verifyComplete();
        StepVerifier.create(authenticationMono).expectNext(new BearerTokenAuthenticationToken(TOKEN)).verifyComplete();
    }

}