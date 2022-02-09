package com.nimvb.app.discord.security.provider;

import com.nimvb.app.discord.security.exception.AccountDisabledException;
import com.nimvb.app.discord.security.exception.AccountLockedException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RequiredArgsConstructor
public class ReactiveUsernamePasswordAuthenticationProvider implements ReactiveAuthenticationProvider {

    @NonNull
    private final ReactiveUserDetailsService userDetailsService;

    @NonNull
    private final PasswordEncoder encoder;

    @Setter
    @NonNull
    private UserDetailsChecker preAuthenticationChecker = this::preAuthenticationChecker;

    @Setter
    @NonNull
    private UserDetailsChecker postAuthenticationChecker = this::postAuthenticationChecker;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {


        return Mono.fromSupplier(() -> Mono.just(authentication).cast(UsernamePasswordAuthenticationToken.class))
                .flatMap(tokenMono -> tokenMono)
                .flatMap(usernamePasswordAuthenticationToken -> userDetailsService.findByUsername(usernamePasswordAuthenticationToken.getName()))
                .transformDeferredContextual((userDetailsMono, contextView) -> {
                    Optional<UsernamePasswordAuthenticationToken> token = contextView.getOrEmpty("token");
                    if (token.isEmpty()) {
                        return Mono.empty();
                    }
                    return userDetailsMono.filter(userDetails -> encoder.matches(token.get().getCredentials().toString(), userDetails.getPassword()));
                })
                .doOnNext(userDetails -> preAuthenticationChecker.check(userDetails))
                .map(userDetails -> {
                    return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
                            userDetails.getAuthorities());
                })
                .doOnNext(token -> postAuthenticationChecker.check(((UserDetails) token.getPrincipal())))
                .cast(Authentication.class)
                .contextWrite(context -> context.put("token", authentication))
                .onErrorResume(throwable -> Mono.empty())
                .switchIfEmpty(Mono.error(new BadCredentialsException("invalid username or password")));
    }

    @Override
    public Mono<Boolean> supports(Class<?> authentication) {
        return Mono.just(false)
                .flatMap(supports -> {
                    if (authentication == null) {
                        return Mono.just(false);
                    }
                    return Mono.just(UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
                });
    }


    private void preAuthenticationChecker(UserDetails user){
//        if (!user.isAccountNonLocked()) {
//            throw new AccountLockedException("account is locked");
//        }
//        if (!user.isEnabled()) {
//            throw new AccountDisabledException("account is disabled");
//        }
//        if (!user.isAccountNonExpired()) {
//            throw new AccountExpiredException("account is expired");
//        }
    }

    private void postAuthenticationChecker(UserDetails user){

    }
}
