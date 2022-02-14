package com.nimvb.app.discord.service;

import com.nimvb.app.discord.domain.User;
import com.nimvb.app.discord.exception.UserNotFoundException;
import com.nimvb.app.discord.exception.UsernameIsAlreadyExistsException;
import com.nimvb.app.discord.repository.UserRepository;
import com.nimvb.app.discord.request.UserRegistrationRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<Void> create(@NonNull UserRegistrationRequest request) {

        return Mono.just(request)
                .map(req -> User.builder()
                        .withUsername(req.getUsername())
                        .withPassword(passwordEncoder.encode(req.getPassword()))
                        .withEmail(req.getEmail())
                        .build())
                .flatMap(userRepository::save)
                .onErrorResume(throwable -> {
                    return Mono.error(
                            new UsernameIsAlreadyExistsException(request.getUsername(),
                                    throwable));
                })
                .then();

    }

    @Override
    public Mono<User> find(@NonNull String username) {
        return userRepository
                .findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)));
    }
}
