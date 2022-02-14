package com.nimvb.app.discord.repository;

import com.nimvb.app.discord.configuration.MongoConfiguration;
import com.nimvb.app.discord.domain.User;
import com.nimvb.app.discord.util.UserBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@Import({ValidationAutoConfiguration.class, MongoConfiguration.class})
@TestPropertySource(properties = {
        "spring.mongodb.embedded.version=3.5.5",
        "spring.data.mongodb.auto-index-creation=true"
})
@DirtiesContext
class UserRepositoryTest {

    @Autowired
    ReactiveMongoTemplate template;

    @Autowired
    ValidatingMongoEventListener validatingMongoEventListener;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    public void init() {
        template
                .getCollectionNames()
                .filter(collectionName -> collectionName.contains("users"))
                .switchIfEmpty(
                        template
                                .createCollection("users")
                                .thenReturn("users"))
                .blockFirst();


    }

    @AfterEach
    public void destroy() {
        template.dropCollection("users").block();
    }

    @Test
    void ShouldReturnTheUserWhenTheUsernameIsProvided(){
        User user = UserBuilder.build("username","password","email@email.com");
        template.save(user).block();


        final Mono<User> target = userRepository.findByUsername("username");
        final Mono<User> nothing = userRepository.findByUsername("other");
        StepVerifier.create(target)
                .expectNextCount(1)
                .expectComplete()
                .verify();

        StepVerifier.create(target)
                .expectNext(user)
                .expectComplete()
                .verify();

        StepVerifier.create(nothing)
                .expectNextCount(0)
                .expectComplete()
                .verify();



    }

    @Test
    void ShouldReturnTheUserWhenTheEmailIsProvided(){
        User user = UserBuilder.build("username","password","email@email.com");
        template.save(user).block();


        final Mono<User> target = userRepository.findByEmail("email@email.com");
        final Mono<User> nothing = userRepository.findByEmail("other");
        StepVerifier.create(target)
                .expectNextCount(1)
                .expectComplete()
                .verify();

        StepVerifier.create(target)
                .expectNext(user)
                .expectComplete()
                .verify();

        StepVerifier.create(nothing)
                .expectNextCount(0)
                .expectComplete()
                .verify();



    }

}