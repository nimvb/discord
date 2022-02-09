package com.nimvb.app.discord.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimvb.app.discord.configuration.TestClockConfiguration;
import com.nimvb.app.discord.model.AccessToken;
import com.nimvb.app.discord.security.service.SecretProvider;
import com.nimvb.app.discord.security.util.Jwe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AccessTokenConverterOfUsernamePasswordAuthentication.class,TestClockConfiguration.class})
class AccessTokenConverterOfUsernamePasswordAuthenticationTest {

    @MockBean
    SecretProvider secretProvider;

    @Autowired
    Clock clock;

    @Autowired
    AccessTokenConverterOfUsernamePasswordAuthentication converter;

    @BeforeEach
    void init(){

        Mockito.when(secretProvider.secret()).thenReturn(SecretProvider.DEFAULT_SECRET);
    }

    @Test
    void ShouldThrownAnExceptionWhenNULLTokenIsProvided(){
        Assertions.assertThatThrownBy(() -> {
            converter.convert(null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    void ShouldReturnMonoWithAccessTokenWhenValidTokenIsProvided(){

            final Instant now = Instant.now(clock);
            final Instant accessTokenTime = now.plus(1,ChronoUnit.HOURS);
            final Instant refreshTokenTime = now.plus(24,ChronoUnit.HOURS);
            final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("username","password", Collections.emptyList());
            final Mono<AccessToken> accessTokenMono = converter.convert(token);

            StepVerifier.create(accessTokenMono).expectNextCount(1).verifyComplete();
            StepVerifier.create(accessTokenMono).expectNextMatches(accessToken -> {
                AtomicReference<JWTClaimsSet> accessTokenClaimsReference = new AtomicReference<>();
                AtomicReference<JWTClaimsSet> refreshTokenClaimsReference = new AtomicReference<>();
                boolean accessTokenCheck = false;
                boolean refreshTokenCheck = false;
                Assertions.assertThatCode(() -> {
                    accessTokenClaimsReference.set(Jwe.require(Jwe.Algorithm.AES128HS256(secretProvider.secret())).build().decrypt(accessToken.getAccessToken()));
                    refreshTokenClaimsReference.set(Jwe.require(Jwe.Algorithm.AES128HS256(secretProvider.secret())).build().decrypt(accessToken.getRefreshToken()));
                }).doesNotThrowAnyException();
                if (accessTokenClaimsReference.get() != null) {
                    final JWTClaimsSet accessTokenClaims = accessTokenClaimsReference.get();
                    final boolean subjectCheck = accessTokenClaims.getSubject() != null && accessTokenClaims.getSubject().equals("username");
                    final boolean issuerCheck = accessTokenClaims.getIssuer() != null && accessTokenClaims.getIssuer().equals("users");
                    final boolean expiredAtCheck = accessTokenClaims.getExpirationTime().equals(Date.from(accessTokenTime));//true;//accessTokenClaims.getIssuer() != null && accessTokenClaims.getIssuer().equals("user");
                    accessTokenCheck = subjectCheck && issuerCheck && expiredAtCheck;
                }
                if(refreshTokenClaimsReference.get() != null){
                    final JWTClaimsSet refreshTokenClaims = refreshTokenClaimsReference.get();
                    final boolean subjectCheck = refreshTokenClaims.getSubject() != null && refreshTokenClaims.getSubject().equals("username");
                    final boolean issuerCheck = refreshTokenClaims.getIssuer() != null && refreshTokenClaims.getIssuer().equals("users");
                    final boolean expiredAtCheck = refreshTokenClaims.getExpirationTime().equals(Date.from(refreshTokenTime));//refreshTokenClaims.getExpirationTime() != null && refreshTokenClaims.getExpirationTime().equals("1");
                    refreshTokenCheck = subjectCheck && issuerCheck && expiredAtCheck;
                }
                return accessTokenCheck && refreshTokenCheck;
            }).verifyComplete();



    }

}