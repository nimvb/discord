package com.nimvb.app.discord.security.service;

import com.nimvb.app.discord.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
public class ReactiveCustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserService userService;

    @Autowired
    public ReactiveCustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }


    @Override
    public Mono<UserDetails> findByUsername(String username) {
        if(username == null){
            return Mono.empty();
        }
        return Mono.just(username)
                .flatMap(s -> userService.find(username))
                .map(user -> User.withUsername(username)
                        .password(user.getPassword())
                        .authorities(user.getRoles().toArray(new String[0]))
                        .build())
                .onErrorResume(throwable -> Mono.empty());
    }
}
