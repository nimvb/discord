package com.nimvb.app.discord.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimvb.app.discord.security.resource.BearerTokenAuthenticationToken;
import com.nimvb.app.discord.security.util.Jwe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@WebFluxTest(excludeFilters = {@ComponentScan.Filter(RestController.class)})
@AutoConfigureWebTestClient(timeout = "PT60M")
@Import(value = {TestEncodingConfiguration.class, SecurityOfPrivateResourcesConfiguration.class,TestClockConfiguration.class})
class SecurityOfPrivateResourcesConfigurationAuthorizationTest {

    @MockBean
    ReactiveAuthenticationManager authenticationManager;

    @Autowired
    Clock clock;


    @Autowired
    WebTestClient client;

    private static final String RELATIVE_URL = "/foo/bar";
    private static final String AUTHENTICATION_RELATIVE_URL = "/api/v1/authenticate";

    private static final String SECRET = "SECRET";


    @BeforeEach
    void init() {
        Mockito.when(authenticationManager.authenticate(ArgumentMatchers.any(Authentication.class)))
                .thenAnswer(invocationOnMock -> {
                    Authentication authentication = invocationOnMock.getArgument(0);
                    if (!(authentication instanceof BearerTokenAuthenticationToken)) {
                        if(authentication instanceof UsernamePasswordAuthenticationToken){
                            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
                            if(!token.getName().isBlank() && token.getCredentials().equals("password")){
                                return Mono.just(new UsernamePasswordAuthenticationToken(token.getName(),token.getCredentials(),Collections.emptyList()));
                            }else{
                                return Mono.error(new BadCredentialsException("invalid credentials"));
                            }

                        }
                        return Mono.empty();
                    }
                    BearerTokenAuthenticationToken token = (BearerTokenAuthenticationToken) authentication;
                    return Mono.just(token)
                            .zipWith(Mono.fromCallable(() -> Jwe.Algorithm.AES128HS256(SECRET)))
                            .flatMap(tuple -> {
                                String jwt = tuple.getT1().getToken();
                                return Mono.fromCallable(() -> Jwe.require(tuple.getT2()).build().decrypt(jwt));
                            })
                            .map(claimsSet -> {
                                // TODO: Careful to update the test in a way to manage the roles
                                return new BearerTokenAuthenticationToken(token.getToken(), claimsSet.getSubject(), Collections.emptyList());
                            })
                            .onErrorResume(throwable -> Mono.error(new BadCredentialsException("invalid token")));
                });
    }

    @Test
    void ShouldReturnAnUnauthorizedResponseWhenTheRequestDoseNotContainAnyValidBearerToken() {
        client.get()
                .uri(RELATIVE_URL)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }


    @Test
    void ShouldReturnNotFoundResponseWhenTheRequestDoseNotContainAnyValidBearerToken() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {

        String jwe = Jwe.create()
                .withSubject("username")
                .withExpiredAt(Date.from(Instant.now(clock).plus(1, ChronoUnit.HOURS)))
                .encrypt(Jwe.Algorithm.AES128HS256(SECRET));
        client.get()
                .uri(RELATIVE_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwe)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void ShouldNotValidateBearerTokensWhenTheRequestsMatchesTheAuthenticationUrl() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        String jwe = Jwe.create()
                .withSubject("username")
                .withExpiredAt(Date.from(Instant.now(clock).plus(1, ChronoUnit.HOURS)))
                .encrypt(Jwe.Algorithm.AES128HS256(SECRET));
        client.post()
                .uri(AUTHENTICATION_RELATIVE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwe)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void ShouldValidateJustTheUsernamePasswordTokensWhenTheRequestsMatchesTheAuthenticationUrl() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        String jwe = Jwe.create()
                .withSubject("username")
                .withExpiredAt(Date.from(Instant.now(clock).plus(1, ChronoUnit.HOURS)))
                .encrypt(Jwe.Algorithm.AES128HS256(SECRET));
        BodyInserters.FormInserter<String> form = BodyInserters.fromFormData("username", "username")
                .with("password", "password");
        client.post()
                .uri(AUTHENTICATION_RELATIVE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwe)
                .body(form)
                .exchange()
                .expectStatus()
                .isNotFound();
    }


}