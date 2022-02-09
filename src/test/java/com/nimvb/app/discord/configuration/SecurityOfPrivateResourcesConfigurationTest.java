package com.nimvb.app.discord.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimvb.app.discord.controller.RegistrationController;
import com.nimvb.app.discord.security.service.SecretProvider;
import com.nimvb.app.discord.security.util.Jwe;
import com.nimvb.app.discord.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebFilter;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Date;
import java.util.List;


@ExtendWith(SpringExtension.class)
@WebFluxTest(excludeFilters = {@ComponentScan.Filter(RestController.class)})
@AutoConfigureWebTestClient(timeout = "PT60M")
@Import(value = {TestEncodingConfiguration.class, SecurityOfPrivateResourcesConfiguration.class})
//@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
class SecurityOfPrivateResourcesConfigurationTest {


    @MockBean
    ReactiveAuthenticationManager authenticationManager;

//    @MockBean
//    SecretProvider secretProvider;
//
//    @MockBean
//    UserService userService;
//
//    @MockBean
//    ReactiveUserDetailsService reactiveUserDetailsService;
//
//    @Autowired
//    SecurityWebFilterChain securityWebFilterChain;
//
//    @Autowired
//    ApplicationContext applicationContext;

    @Autowired
    WebTestClient client;

//    @BeforeEach
//    void init(){
//        Mockito.when(secretProvider.secret()).thenReturn(SecretProvider.DEFAULT_SECRET);
//    }

    @Test
    void ShouldProvideSecurityWebFilterChainBeanForPrivateResources(){

        new ApplicationContextRunner()
                .withBean(ReactiveAuthenticationManager.class,() -> authenticationManager,bd -> {})
                .withUserConfiguration(SecurityOfPrivateResourcesConfiguration.class)
                .run(context -> {
            Assertions.assertThat(context).hasSingleBean(SecurityWebFilterChain.class);
        });
    }


    @Test
    void ShouldReturnResponseWithUnauthorizedStatusCodeWhenTheRequestIsGiven(){
        client.post()
                .uri("/api/v1/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody();
    }



//    @Test
//    void a() throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
//
//        final List<WebFilter> block = securityWebFilterChain.getWebFilters().collectList().block();
//
//        client.get()
//                .uri("/api/v1/users/")
//                .header(HttpHeaders.AUTHORIZATION,"Bearer " + Jwe.create().withSubject("sa").withExpiredAt(Date.from(Instant.now().plusSeconds(5000))).encrypt(Jwe.Algorithm.AES128HS256("secret")))
//                .exchange()
//                .expectStatus()
//                .isUnauthorized();
//
//    }
}