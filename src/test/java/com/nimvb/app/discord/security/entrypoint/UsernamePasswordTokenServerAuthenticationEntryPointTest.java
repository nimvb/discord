package com.nimvb.app.discord.security.entrypoint;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class UsernamePasswordTokenServerAuthenticationEntryPointTest {

    private static final String RELATIVE_URL = "/api/v1/custom";

    @Test
    void ShouldReturnEmptyMonoWhenExchangeIsNULL() {

        UsernamePasswordTokenServerAuthenticationEntryPoint entryPoint =
                new UsernamePasswordTokenServerAuthenticationEntryPoint();
        Mono<Void> commence = entryPoint.commence(null, null);
        StepVerifier.create(commence).verifyComplete();

    }

    @Test
    void ShouldReturnCompleteResponseWhenAuthenticationIsNULL() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get(RELATIVE_URL)
                .build());
        UsernamePasswordTokenServerAuthenticationEntryPoint entryPoint =
                new UsernamePasswordTokenServerAuthenticationEntryPoint();
        Mono<Void> commence = entryPoint.commence(exchange, null);
        StepVerifier.create(commence).verifyComplete();
        MockServerHttpResponse response = exchange.getResponse();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.isCommitted()).isEqualTo(true);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        Assertions.assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

}