package com.nimvb.app.discord.service;

import com.nimvb.app.discord.domain.User;
import com.nimvb.app.discord.exception.UserNotFoundException;
import com.nimvb.app.discord.request.UserRegistrationRequest;
import lombok.NonNull;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

public interface UserService {

    Mono<Void> create(@NonNull @Valid UserRegistrationRequest request);

    /**
     * Find the user which has username equals to {@code username}
     * @param username the target username
     * @throws  UserNotFoundException when no user with the provided {@code username} is exists
     * @return the user or {@link Mono#error(Throwable)} if the user is not
     * found
     */
    Mono<User> find(@NonNull String username);

}
