package com.nimvb.app.discord.security.converter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.FormHttpMessageWriter;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Objects;

import static com.nimvb.app.discord.security.converter.ServerUsernamePasswordAuthenticationConverter.SPRING_SECURITY_FORM_PASSWORD_KEY;
import static com.nimvb.app.discord.security.converter.ServerUsernamePasswordAuthenticationConverter.SPRING_SECURITY_FORM_USERNAME_KEY;
import static org.springframework.core.ResolvableType.forClassWithGenerics;


@ExtendWith(SpringExtension.class)
class ServerUsernamePasswordAuthenticationConverterTest {


    private static final MultiValueMap<String,String> FORM_DATA = new LinkedMultiValueMap<>();


    private MockServerWebExchange exchangeWithFormData = null;
    private MockServerWebExchange exchangeWithNoFormData = null;
    private       MockServerWebExchange exchangeWithJsonValue = null;
    private final MockServerWebExchange nullExchange          = null;

    @BeforeAll
    static void config(){
        FORM_DATA.add(SPRING_SECURITY_FORM_USERNAME_KEY,"username");
        FORM_DATA.add(SPRING_SECURITY_FORM_PASSWORD_KEY,"password");
    }

    @BeforeEach
    void init(){

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, "/");
        Void block = new FormHttpMessageWriter().write(Mono.just(FORM_DATA),
                        forClassWithGenerics(MultiValueMap.class, String.class, String.class),
                        MediaType.APPLICATION_FORM_URLENCODED, request, Collections.emptyMap())
                .block();
        exchangeWithFormData =  MockServerWebExchange.from(
                MockServerHttpRequest
                        .post("/")
                        .contentType(Objects.requireNonNull(request.getHeaders().getContentType()))
                        .body(request.getBody()));

        exchangeWithNoFormData = MockServerWebExchange.from(MockServerHttpRequest.post("/").contentType(MediaType.APPLICATION_FORM_URLENCODED).body(""));
        exchangeWithJsonValue = MockServerWebExchange.from(MockServerHttpRequest.post("/").contentType(MediaType.APPLICATION_JSON).body("{}"));
    }



    @Test
    void ShouldReturnEmptyMonoWhenANULLExchangeIsPassed(){
        ServerUsernamePasswordAuthenticationConverter converter = new ServerUsernamePasswordAuthenticationConverter();

        Mono<Authentication> token = converter.convert(nullExchange);

        StepVerifier.create(token).expectNextCount(0).verifyComplete();
    }

    @Test
    void ShouldReturnMonoWithTokenWhenAnExchangeWithNoFormDataIsPassed(){
        ServerUsernamePasswordAuthenticationConverter converter = new ServerUsernamePasswordAuthenticationConverter();

        Mono<Authentication> token = converter.convert(exchangeWithNoFormData);

        StepVerifier.create(token).expectNextCount(1).verifyComplete();
        StepVerifier.create(token).expectNext(new UsernamePasswordAuthenticationToken("","")).verifyComplete();
    }

    @Test
    void ShouldReturnMonoWithTokenWhenAnExchangeWithDifferentContentTypeIsPassed(){
        ServerUsernamePasswordAuthenticationConverter converter = new ServerUsernamePasswordAuthenticationConverter();

        Mono<Authentication> token = converter.convert(exchangeWithJsonValue);

        StepVerifier.create(token).expectNextCount(1).verifyComplete();
        StepVerifier.create(token).expectNext(new UsernamePasswordAuthenticationToken("","")).verifyComplete();
    }


    @Test
    void ShouldReturnMonoWithValidTokenWhenAnExchangeWithFormDataIsPassed(){
        ServerUsernamePasswordAuthenticationConverter converter = new ServerUsernamePasswordAuthenticationConverter();

        Mono<Authentication> token = converter.convert(exchangeWithFormData);

        StepVerifier.create(token).expectNextCount(1).verifyComplete();
        StepVerifier.create(token).expectNext(new UsernamePasswordAuthenticationToken("username","password")).verifyComplete();
    }

}