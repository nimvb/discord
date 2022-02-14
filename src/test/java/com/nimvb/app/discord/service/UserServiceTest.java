package com.nimvb.app.discord.service;

import com.nimvb.app.discord.configuration.MongoConfiguration;
import com.nimvb.app.discord.configuration.TestEncodingConfiguration;
import com.nimvb.app.discord.configuration.TestUserServiceConfiguration;
import com.nimvb.app.discord.domain.User;
import com.nimvb.app.discord.exception.UserNotFoundException;
import com.nimvb.app.discord.exception.UsernameIsAlreadyExistsException;
import com.nimvb.app.discord.repository.UserRepository;
import com.nimvb.app.discord.request.UserRegistrationRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@DataMongoTest()
@Import({ValidationAutoConfiguration.class, MongoConfiguration.class, TestEncodingConfiguration.class, TestUserServiceConfiguration.class})
@TestPropertySource(properties = {
        "spring.mongodb.embedded.version=3.5.5",
        "spring.data.mongodb.auto-index-creation=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    UserRepository repository;

    @Autowired
    UserService userService;
    
    private User sampleUser(){
        return User
                .builder()
                .withUsername("username")
                .withPassword("password")
                .withEmail("email@email.com")
                .build();
    }
    
    private UserRegistrationRequest sampleRegistrationRequest(){
        return new UserRegistrationRequest("username", "password", "email@email.com");
    }

    @Test
    void ShouldThrownAnExceptionWhenNULLPassedAsAnArgument() {
        Assertions.assertThatThrownBy(() -> {
            userService.create(null);
        }).isInstanceOf(NullPointerException.class);

        Assertions.assertThatThrownBy(() -> {
            userService.find(null);
        }).isInstanceOf(NullPointerException.class);
    }


    @Test
    void ShouldCreateAUserWhenTheRequiredArgumentsIsProvided() {
        final Mono<Void> result = userService.create(sampleRegistrationRequest());
        final Mono<User> entity = repository.findByUsername("username");
        StepVerifier.create(result).expectNextCount(0).verifyComplete();
        StepVerifier.create(entity).expectNextCount(1).verifyComplete();
        StepVerifier.create(entity).expectNext(sampleUser()).verifyComplete();
    }

    @Test
    void ShouldThrownAnErrorWhenTheProvidedUsernameIsAlreadyExists() {
        final Mono<Void> user = userService.create(sampleRegistrationRequest());
        final Mono<Void> demand = userService.create(new UserRegistrationRequest("username", "password1", "email1@email.com"));
        final Mono<User> entity = repository.findByUsername("username");
        StepVerifier.create(user).expectNextCount(0).verifyComplete();
        StepVerifier.create(demand).expectError().verify();
        StepVerifier.create(demand).expectErrorMatches(throwable ->
                        UsernameIsAlreadyExistsException.class
                                .isAssignableFrom(throwable.getClass()))
                .verify();
        StepVerifier.create(entity).expectNextCount(1).verifyComplete();
        StepVerifier.create(entity).expectNext(sampleUser()).verifyComplete();
    }


    @Test
    void ShouldFindUserWhenTheUsernameWhichIsAlreadyPersistedIsProvided(){
        User       entity = sampleUser();
        Mono<User> user   = repository.save(entity);
        StepVerifier.create(user).expectNextCount(1).verifyComplete();

        Mono<User> target = userService.find("username");
        StepVerifier.create(target).expectNextCount(1).verifyComplete();
        StepVerifier.create(target).expectNext(entity).verifyComplete();
    }


    @Test
    void ShouldThrownAnExceptionWhenTheUsernameWhichIsNotExistsIsProvided(){
        User       entity = sampleUser();
        Mono<User> user   = repository.save(entity);
        StepVerifier.create(user).expectNextCount(1).verifyComplete();

        Mono<User> target = userService.find("fake");
        StepVerifier.create(target).expectNextCount(0).verifyError();
        StepVerifier.create(target).expectError(UserNotFoundException.class).verify();
    }


}