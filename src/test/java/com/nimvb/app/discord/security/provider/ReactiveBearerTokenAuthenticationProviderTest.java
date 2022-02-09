package com.nimvb.app.discord.security.provider;

import com.nimbusds.jose.JOSEException;
import com.nimvb.app.discord.security.resource.BearerTokenAuthenticationToken;
import com.nimvb.app.discord.security.service.SecretProvider;
import com.nimvb.app.discord.security.util.Jwe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ExtendWith(SpringExtension.class)
class ReactiveBearerTokenAuthenticationProviderTest {


    @MockBean
    SecretProvider secretProvider;


    @BeforeEach
    void init(){
        Mockito.when(secretProvider.secret()).thenReturn("secret");
    }

    @Test
    void ShouldThrownExceptionWhenInvalidSecretProviderIsProvided(){
        Assertions.assertThatThrownBy(() -> {
            new ReactiveBearerTokenAuthenticationProvider(null);
        }).isInstanceOf(NullPointerException.class);
    }



    @Test
    void ShouldReturnFalseWhenNULLTypeIsGiven(){
        ReactiveBearerTokenAuthenticationProvider provider = new ReactiveBearerTokenAuthenticationProvider(secretProvider);
        final Mono<Boolean> supports = provider.supports(null);
        StepVerifier.create(supports).expectNext(false).verifyComplete();
    }


    @Test
    void ShouldReturnFalseWhenInvalidTypeIsGiven(){
        ReactiveBearerTokenAuthenticationProvider provider = new ReactiveBearerTokenAuthenticationProvider(secretProvider);
        final Mono<Boolean> supports = provider.supports(UsernamePasswordAuthenticationToken.class);
        StepVerifier.create(supports).expectNext(false).verifyComplete();
    }

    @Test
    void ShouldReturnTrueWhenValidTypeIsGiven(){
        ReactiveBearerTokenAuthenticationProvider provider = new ReactiveBearerTokenAuthenticationProvider(secretProvider);
        final Mono<Boolean> supports = provider.supports(BearerTokenAuthenticationToken.class);
        StepVerifier.create(supports).expectNext(true).verifyComplete();
    }


    @Test
    void ShouldReturnMonoWithErrorWhenNULLArgumentIsGiven(){
        ReactiveBearerTokenAuthenticationProvider provider = new ReactiveBearerTokenAuthenticationProvider(secretProvider);
        final Mono<Authentication> authenticate = provider.authenticate(null);
        StepVerifier.create(authenticate).expectError(AuthenticationException.class).verify();
        StepVerifier.create(authenticate).expectError(BadCredentialsException.class).verify();
        StepVerifier.create(authenticate).expectErrorMessage("invalid token").verify();
    }

    @Test
    void ShouldReturnMonoThatProvidesAnErrorWhenInvalidTokenTypeIsGiven(){
        ReactiveBearerTokenAuthenticationProvider provider = new ReactiveBearerTokenAuthenticationProvider(secretProvider);
        final Mono<Authentication> authenticate = provider.authenticate(new UsernamePasswordAuthenticationToken("username","password"));
        StepVerifier.create(authenticate).expectError(AuthenticationException.class).verify();
        StepVerifier.create(authenticate).expectError(BadCredentialsException.class).verify();
        StepVerifier.create(authenticate).expectErrorMessage("invalid token").verify();
    }


    @Test
    void ShouldReturnMonoThatProvidesAuthenticatedTokenWhenAValidTokenIsProvided() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        ReactiveBearerTokenAuthenticationProvider provider = new ReactiveBearerTokenAuthenticationProvider(secretProvider);
        final Instant now = Instant.now();
        final String jwe = Jwe
                .create()
                .withSubject("subject")
                .withIssuer("issuer")
                .withIssuedAt(Date.from(now))
                .withExpiredAt(Date.from(now.plusSeconds(60 * 5)))
                .encrypt(Jwe.Algorithm.AES128HS256(secretProvider.secret()));
        final Mono<Authentication> authenticate = provider.authenticate(new BearerTokenAuthenticationToken(jwe));
        StepVerifier.create(authenticate).expectNextCount(1).verifyComplete();
        StepVerifier.create(authenticate).assertNext(token -> {
            Assertions.assertThat(token)
                    .isNotNull()
                    .isInstanceOf(BearerTokenAuthenticationToken.class)
                    .isEqualTo(new BearerTokenAuthenticationToken(jwe,"",Collections.emptyList()));
        }).verifyComplete();
    }

    @Test
    void ShouldReturnMonoThatProvidesAuthenticatedTokenWhenAValidTokenWithExtraRolesClaimIsProvided() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        ReactiveBearerTokenAuthenticationProvider provider = new ReactiveBearerTokenAuthenticationProvider(secretProvider);
        final Instant now = Instant.now();
        final String jwe = Jwe
                .create()
                .withSubject("subject")
                .withIssuer("issuer")
                .withClaim("roles", List.of("user","admin"))
                .withIssuedAt(Date.from(now))
                .withExpiredAt(Date.from(now.plusSeconds(60 * 5)))
                .encrypt(Jwe.Algorithm.AES128HS256(secretProvider.secret()));
        final Mono<Authentication> authenticate = provider.authenticate(new BearerTokenAuthenticationToken(jwe));
        StepVerifier.create(authenticate).expectNextCount(1).verifyComplete();
        StepVerifier.create(authenticate).assertNext(token -> {
            Assertions.assertThat(token)
                    .isNotNull()
                    .isInstanceOf(BearerTokenAuthenticationToken.class)
                    .isEqualTo(new BearerTokenAuthenticationToken(jwe,"", Stream.of("user","admin").map(SimpleGrantedAuthority::new).collect(Collectors.toList())));
        }).verifyComplete();
    }


    @Test
    void ShouldReturnMonoWithErrorWhenAnExpiredValidTokenIsProvided() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        ReactiveBearerTokenAuthenticationProvider provider = new ReactiveBearerTokenAuthenticationProvider(secretProvider);
        final Instant now = Instant.now().minusSeconds(60);
        final String jwe = Jwe
                .create()
                .withSubject("subject")
                .withIssuer("issuer")
                .withIssuedAt(Date.from(now))
                .withExpiredAt(Date.from(now.plusSeconds(30)))
                .encrypt(Jwe.Algorithm.AES128HS256(secretProvider.secret()));
        final Mono<Authentication> authenticate = provider.authenticate(new BearerTokenAuthenticationToken(jwe));
        StepVerifier.create(authenticate).expectError(AuthenticationException.class).verify();
        StepVerifier.create(authenticate).expectError(BadCredentialsException.class).verify();
        StepVerifier.create(authenticate).expectErrorMessage("invalid token").verify();
    }

    @Test
    void ShouldReturnMonoWithErrorWhenAnValidTokenWithInvalidKeyIsProvided() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        ReactiveBearerTokenAuthenticationProvider provider = new ReactiveBearerTokenAuthenticationProvider(secretProvider);
        final Instant now = Instant.now();
        final String jwe = Jwe
                .create()
                .withSubject("subject")
                .withIssuer("issuer")
                .withIssuedAt(Date.from(now))
                .withExpiredAt(Date.from(now.plusSeconds(60 * 5)))
                .encrypt(Jwe.Algorithm.AES128HS256(secretProvider.secret() + secretProvider.secret()));
        final Mono<Authentication> authenticate = provider.authenticate(new BearerTokenAuthenticationToken(jwe));
        StepVerifier.create(authenticate).expectError(AuthenticationException.class).verify();
        StepVerifier.create(authenticate).expectError(BadCredentialsException.class).verify();
        StepVerifier.create(authenticate).expectErrorMessage("invalid token").verify();
    }

}