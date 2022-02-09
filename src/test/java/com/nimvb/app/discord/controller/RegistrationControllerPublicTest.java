package com.nimvb.app.discord.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimvb.app.discord.domain.User;
import com.nimvb.app.discord.exception.UserNotFoundException;
import com.nimvb.app.discord.exception.UsernameIsAlreadyExistsException;
import com.nimvb.app.discord.request.UserRegistrationRequest;
import com.nimvb.app.discord.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {RegistrationController.class}, excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
@AutoConfigureWebTestClient(timeout = "PT15M")
class RegistrationControllerPublicTest {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    @MockBean
    UserService userService;

    @Autowired
    WebTestClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void init() {

        users.clear();

        Mockito.when(userService.create(ArgumentMatchers.any())).thenAnswer(invocationOnMock -> {
            UserRegistrationRequest request = (UserRegistrationRequest) invocationOnMock.getArgument(0);
            if(users.containsKey(request.getUsername())){
                return Mono.error(new UsernameIsAlreadyExistsException(request.getUsername(),null));
            }
            User user = new User(request.getUsername(), request.getPassword(), request.getEmail());
            users.put(request.getUsername(), user);
            return Mono.just(user)
                    .then();
        });

        Mockito.when(userService.find(ArgumentMatchers.anyString())).thenAnswer(invocationOnMock -> {
            String username = (String) invocationOnMock.getArgument(0);
            if(!users.containsKey(username)){
                return Mono.error(new UserNotFoundException(""));
            }
            return Mono.just(users.get(username));
        });

    }





    @Test
    void ShouldReturnResponseWithBadRequestAsStatusCodeWhenTheRequestBodyIsNotValid() throws JsonProcessingException {

        ObjectNode bodyWithInvalidData = mapper.createObjectNode()
                .put("username", "")
                .put("password", "")
                .put("email", "");

        ObjectNode bodyWithInvalidStructure = mapper.createObjectNode()
                .put("usernam", "username")
                .put("passwor", "password")
                .put("emai", "email@email.com");

        client.post()
                .uri("/api/v1/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody();

        client.post()
                .uri("/api/v1/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(bodyWithInvalidData))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody();

        client.post()
                .uri("/api/v1/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(bodyWithInvalidStructure))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody();
    }


    @Test
    void ShouldReturnResponseWithInvalidAsStatusCodeWhenTheRequestContentTypeIsNotValid() throws JsonProcessingException {

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
                .is4xxClientError()
                .expectBody();

        Mono<User> user = userService.find("username");
        StepVerifier.create(user).expectError().verify();

    }


    @Test
    void ShouldReturnResponseWithCreatedAsStatusCodeWhenTheRequestBodyIsValid() throws JsonProcessingException {

        ObjectNode bodyWithValidData = mapper.createObjectNode()
                .put("username", "username")
                .put("password", "password")
                .put("email", "email@email.com");



        client.post()
                .uri("/api/v1/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(bodyWithValidData))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .isEmpty();

        Mono<User> user = userService.find("username");
        StepVerifier.create(user).expectNextCount(1).verifyComplete();

    }


    @Test
    void ShouldReturnResponseWithErrorAsStatusCodeWhenTheRequestBodyIsNotValidInTermOfBusinessLogic() throws JsonProcessingException {

        ObjectNode bodyWithValidData = mapper.createObjectNode()
                .put("username", "username")
                .put("password", "password")
                .put("email", "email@email.com");



        client.post()
                .uri("/api/v1/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(bodyWithValidData))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .isEmpty();

        client.post()
                .uri("/api/v1/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(bodyWithValidData))
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody();

        Mono<User> user = userService.find("username");
        StepVerifier.create(user).expectNextCount(1).verifyComplete();

    }


}