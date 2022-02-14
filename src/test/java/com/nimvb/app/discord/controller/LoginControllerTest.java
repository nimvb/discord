package com.nimvb.app.discord.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimvb.app.discord.configuration.SecurityOfPrivateResourcesConfiguration;
import com.nimvb.app.discord.configuration.SecurityOfPublicResourcesConfiguration;
import com.nimvb.app.discord.configuration.TestClockConfiguration;
import com.nimvb.app.discord.domain.User;
import com.nimvb.app.discord.security.service.SecretProvider;
import com.nimvb.app.discord.service.AccessTokenConverterOfUsernamePasswordAuthentication;
import com.nimvb.app.discord.util.UserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;


@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {LoginController.class})
@Import({
        SecurityOfPublicResourcesConfiguration.class,
        SecurityOfPrivateResourcesConfiguration.class,
        TestClockConfiguration.class,
        AccessTokenConverterOfUsernamePasswordAuthentication.class})
@AutoConfigureWebTestClient(timeout = "PT15M")
class LoginControllerTest {


    private static final Map<String, User> USERS = Map.of(
            "username", UserBuilder.build("username","password","email@email.com"),
            "anotheruser",UserBuilder.build("anotheruser","password","email@email.com")
    );

    @MockBean
    SecretProvider secretProvider;

    @MockBean
    ReactiveAuthenticationManager reactiveAuthenticationManager;

    @Autowired
    WebTestClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String RELATIVE_URL = "/api/v1/authenticate";

    @BeforeEach
    void init(){
        Mockito.when(secretProvider.secret()).thenReturn(SecretProvider.DEFAULT_SECRET);
        Mockito.when(reactiveAuthenticationManager.authenticate(ArgumentMatchers.any(Authentication.class))).thenAnswer(invocation -> {
            final Authentication argument = invocation.getArgument(0);
            if(!(argument instanceof UsernamePasswordAuthenticationToken)){
                return Mono.empty();
            }
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) argument;
            return Mono.just(token)
                    .flatMap(t -> {
                        if(!USERS.containsKey(t.getName())){
                            return Mono.error(new BadCredentialsException("invalid credentials"));
                        }
                        final User user = USERS.get(t.getName());
                        if(!user.getPassword().equals(t.getCredentials())){
                            return Mono.error(new BadCredentialsException("invalid credentials"));
                        }
                        return Mono.just(new UsernamePasswordAuthenticationToken(t.getName(),t.getCredentials(), Collections.emptyList()));
                    });

        });
    }

    @Test
    void ShouldReturnResponseWithUnauthorizedStatusCodeWhenARequestSentViaAnyMethodOtherThanPost() {
        client.get()
                .uri(RELATIVE_URL)
                .exchange()
                .expectStatus()
                .isUnauthorized();
        client.put()
                .uri(RELATIVE_URL)
                .exchange()
                .expectStatus()
                .isUnauthorized();
        client.delete()
                .uri(RELATIVE_URL)
                .exchange()
                .expectStatus()
                .isUnauthorized();
        client.head()
                .uri(RELATIVE_URL)
                .exchange()
                .expectStatus()
                .isUnauthorized();
        client.patch()
                .uri(RELATIVE_URL)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void ShouldReturnResponseWithOkStatusCodeWhenARequestOfTypeOptionsIsReceived() {
        client.options()
                .uri(RELATIVE_URL)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void ShouldReturnResponseWithUnauthorizedStatusCodeWhenTheRequestHasAnyContentTypeOtherThanAPPLICATION_FORM_URLENCODED(){



        client.post()
                .uri(RELATIVE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }


    @Test
    void ShouldReturnResponseWithUnauthorizedStatusCodeWhenTheRequestHasInvalidCredentials(){

        BodyInserters.FormInserter<String> formData = BodyInserters
                .fromFormData("username", "usernamed")
                .with("password", "password1");

        client.post()
                .uri(RELATIVE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void ShouldReturnResponseWithOkStatusCodeAndProperResponseWhenTheRequestHasValidCredentials(){

        BodyInserters.FormInserter<String> formData = BodyInserters
                .fromFormData("username", "username")
                .with("password", "password");

        client.post()
                .uri(RELATIVE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON).expectBody()
                .jsonPath("$.access_token").isNotEmpty()
                .jsonPath("$.type").isNotEmpty()
                .jsonPath("$.expirationTime").isNumber()
                .jsonPath("$.refresh_token").isNotEmpty()
                .jsonPath("$.scope").hasJsonPath();
    }
}
