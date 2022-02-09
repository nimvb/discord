package com.nimvb.app.discord.security.manager;

import com.nimvb.app.discord.security.provider.ReactiveAuthenticationProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractUserDetailsReactiveAuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@RequiredArgsConstructor
public class CustomReactiveAuthenticationManager implements ReactiveAuthenticationManager {


    @NonNull private final Set<ReactiveAuthenticationProvider> providers;


    private CustomReactiveAuthenticationManager(Builder builder){
        providers = builder.providers;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if(authentication == null){
            return Mono.error(new BadCredentialsException("invalid credential"));
        }
        return retrieveProviders(authentication)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(provider -> provider.authenticate(authentication))
                .next()
                .onErrorResume(throwable -> Mono.defer(() -> Mono.error(new BadCredentialsException("invalid credentials"))))
                .switchIfEmpty(Mono.empty());

    }

    public static Builder builder(){
        return new Builder();
    }




    private Flux<ReactiveAuthenticationProvider> retrieveProviders(Authentication authentication){
        return Flux.fromIterable(providers)
                .flatMap(provider -> provider.supports(authentication.getClass())
                        .filter(aBoolean -> aBoolean)
                        .map(aBoolean -> provider));
    }

    public static final class Builder {

        private final Set<ReactiveAuthenticationProvider> providers = new HashSet<>();


        public Builder add(@NonNull ReactiveAuthenticationProvider provider){
            providers.add(provider);
            return this;
        }

        public Builder clear(){
            providers.clear();
            return this;
        }

        public ReactiveAuthenticationManager build(){
            return new CustomReactiveAuthenticationManager(this);
        }

    }
}
