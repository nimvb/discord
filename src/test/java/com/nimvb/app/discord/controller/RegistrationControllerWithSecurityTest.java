package com.nimvb.app.discord.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimvb.app.discord.configuration.SecurityOfPrivateResourcesConfiguration;
import com.nimvb.app.discord.configuration.SecurityOfPublicResourcesConfiguration;
import com.nimvb.app.discord.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {RegistrationController.class})
@Import({SecurityOfPublicResourcesConfiguration.class, SecurityOfPrivateResourcesConfiguration.class})
@AutoConfigureWebTestClient(timeout = "PT15M")
class RegistrationControllerWithSecurityTest {

    @MockBean
    UserService userService;

    @MockBean
    ReactiveAuthenticationManager authenticationManager;

    @Autowired
    WebTestClient client;

    private final ObjectMapper mapper = new ObjectMapper();



    @Test
    void ShouldReturnTheExactSameResponseCodeWhenSecurityIsEnabled() throws JsonProcessingException {

        ObjectNode bodyWithValidData = mapper.createObjectNode()
                .put("username", "username")
                .put("password", "password")
                .put("email", "email@email.com");


        client.post()
                .uri("/api/v1/users/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(mapper.writeValueAsString(bodyWithValidData))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .expectBody();

    }

    @Test
    void ShouldReturnOkWhenAnOptionsRequestIsSent(){
        client.options()
                .uri("/api/v1/users/")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void ShouldReturnResponseWithUnAuthorizedStatusCodeWhenAnyRequestOtherThanPostIsSent(){
        client.get()
                .uri("/api/v1/users/")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        client.put()
                .uri("/api/v1/users/")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        client.delete()
                .uri("/api/v1/users/")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        client.patch()
                .uri("/api/v1/users/")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        client.head()
                .uri("/api/v1/users/")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }


}