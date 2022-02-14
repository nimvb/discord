package com.nimvb.app.discord.security.service;

import com.nimvb.app.discord.domain.User;
import com.nimvb.app.discord.exception.UserNotFoundException;
import com.nimvb.app.discord.service.UserService;
import com.nimvb.app.discord.util.UserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

@ExtendWith({SpringExtension.class})
@SpringBootTest()
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
class ReactiveCustomUserDetailsServiceTest {

    private final static List<User> USERS = List
            .of(
                    UserBuilder.build("username", "password", "email@email.com"),
                    UserBuilder.build("other", "password", "other@email.com")
            );

    @MockBean
    UserService userService;


    ReactiveUserDetailsService userDetailsService;


    @BeforeEach
    void init() {

        Mockito.when(userService.find(ArgumentMatchers.anyString())).thenAnswer(invocation -> {
            final String username = invocation.getArgument(0);
            return Flux.fromIterable(USERS)
                    .filter(user -> user.getUsername().equals(username))
                    .next()
                    .switchIfEmpty(Mono.error(new UserNotFoundException(username)));
        });

        userDetailsService = new ReactiveCustomUserDetailsService(userService);
    }


    @Test
    void ShouldReturnEmptyMonoWhenInvalidArgumentIsProvided() {
        final Mono<UserDetails> userDetails = userDetailsService.findByUsername(null);
        StepVerifier.create(userDetails).expectNextCount(0).verifyComplete();
    }

    @Test
    void ShouldReturnProperUserDetailsWhenValidUsernameIsProvided() {
        final Mono<UserDetails> userDetails = userDetailsService.findByUsername("username");
        StepVerifier.create(userDetails).expectNextCount(1).verifyComplete();
        StepVerifier.create(userDetails).expectNext(org.springframework.security.core.userdetails.User
                .withUsername("username")
                .password("password")
                .authorities(Collections.emptyList())
                .build()
        ).verifyComplete();
    }


    @Test
    void ShouldReturnEmptyMonoWhenInvalidUsernameIsProvided() {
        final Mono<UserDetails> userDetails = userDetailsService.findByUsername("fakeusername");
        StepVerifier.create(userDetails).expectNextCount(0).verifyComplete();
    }
}