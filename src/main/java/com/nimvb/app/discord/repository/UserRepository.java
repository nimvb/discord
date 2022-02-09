package com.nimvb.app.discord.repository;

import com.nimvb.app.discord.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, ObjectId> {

    Mono<User> findByUsername(String username);

    Mono<User> findByEmail(String email);
}
