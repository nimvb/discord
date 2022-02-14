package com.nimvb.app.discord.security.provider;

import com.nimvb.app.discord.configuration.TestEncodingConfiguration;
import com.nimvb.app.discord.domain.User;
import com.nimvb.app.discord.security.resource.BearerTokenAuthenticationToken;
import com.nimvb.app.discord.util.UserBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@Import(TestEncodingConfiguration.class)
class ReactiveUsernamePasswordAuthenticationProviderTest {

    private static final List<User> USERS = List.of(
            UserBuilder.build("username", "password", "email@email.com")
    );

    @MockBean
    ReactiveUserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void init() {
        Mockito.when(userDetailsService.findByUsername(ArgumentMatchers.anyString()))
                .thenAnswer(invocationOnMock -> {
                    String argument = invocationOnMock.getArgument(0);
                    return Flux.fromIterable(USERS)
                            .filter(user -> user.getUsername().equals(argument))
                            .next()
                            .map(user -> org.springframework.security.core.userdetails.User
                                    .withUsername(user.getUsername())
                                    .password(user.getPassword())
                                    .authorities(Collections.emptyList())
                                    .build())
                            .switchIfEmpty(Mono.empty());
                });
    }

    @Test
    void ShouldReturnMonoWithErrorWhenTheRequiredConstructorArgumentsIsNULL() {
        Assertions.assertThatThrownBy(() -> {
            new ReactiveUsernamePasswordAuthenticationProvider(null, encoder);
        }).isInstanceOf(NullPointerException.class);

        Assertions.assertThatThrownBy(() -> {
            new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, null);
        }).isInstanceOf(NullPointerException.class);

        Assertions.assertThatThrownBy(() -> {
            new ReactiveUsernamePasswordAuthenticationProvider(null, null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    void ShouldReturnMonoWithErrorWhenTheGivenValuesForPreAuthenticationCheckerOrPostAuthenticationCheckerAreNULL() {
        Assertions.assertThatThrownBy(() -> {
            ReactiveUsernamePasswordAuthenticationProvider provider = new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
            provider.setPreAuthenticationChecker(null);
        }).isInstanceOf(NullPointerException.class);


        Assertions.assertThatThrownBy(() -> {
            ReactiveUsernamePasswordAuthenticationProvider provider = new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
            provider.setPostAuthenticationChecker(null);
        }).isInstanceOf(NullPointerException.class);

    }


    @Test
    void ShouldReturnFalseWhenNULLTypeIsGiven() {
        ReactiveAuthenticationProvider provider = new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
        final Mono<Boolean>            supports = provider.supports(null);
        StepVerifier.create(supports).expectNext(false).verifyComplete();
    }


    @Test
    void ShouldReturnFalseWhenInvalidTypeIsGiven() {
        ReactiveAuthenticationProvider provider = new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
        final Mono<Boolean>            supports = provider.supports(BearerTokenAuthenticationToken.class);
        StepVerifier.create(supports).expectNext(false).verifyComplete();
    }

    @Test
    void ShouldReturnTrueWhenValidTypeIsGiven() {
        ReactiveAuthenticationProvider provider = new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
        final Mono<Boolean>            supports = provider.supports(UsernamePasswordAuthenticationToken.class);
        StepVerifier.create(supports).expectNext(true).verifyComplete();
    }


    @Test
    void ShouldReturnMonoWithErrorWhenNULLIsPassedAsTheAuthenticationToken() {
        ReactiveAuthenticationProvider provider     = new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
        Mono<Authentication>           authenticate = provider.authenticate(null);
        StepVerifier.create(authenticate)
                .expectErrorMessage("invalid username or password")
                .verify();
        StepVerifier.create(authenticate)
                .expectError(BadCredentialsException.class)
                .verify();

    }

    @Test
    void ShouldReturnMonoWithErrorWhenInvalidAuthenticationTokenIsPassedAsAnArgument() {
        ReactiveAuthenticationProvider provider     = new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
        Mono<Authentication>           authenticate = provider.authenticate(new BearerTokenAuthenticationToken("TOKEN"));
        StepVerifier.create(authenticate)
                .expectErrorMessage("invalid username or password")
                .verify();
        StepVerifier.create(authenticate)
                .expectError(BadCredentialsException.class)
                .verify();

    }


    @Test
    void ShouldReturnMonoWithErrorWhenValidAuthenticationTokenWithBadUsernameIsPassedAsAnArgument() {
        ReactiveAuthenticationProvider provider     = new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
        Mono<Authentication>           authenticate = provider.authenticate(new UsernamePasswordAuthenticationToken("user", "pass"));
        StepVerifier.create(authenticate)
                .expectErrorMessage("invalid username or password")
                .verify();
        StepVerifier.create(authenticate)
                .expectError(BadCredentialsException.class)
                .verify();

    }


    @Test
    void ShouldReturnMonoWithErrorWhenValidAuthenticationTokenWithBadPasswordIsPassedAsAnArgument() {
        ReactiveAuthenticationProvider provider     = new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
        Mono<Authentication>           authenticate = provider.authenticate(new UsernamePasswordAuthenticationToken("username", "pass"));
        StepVerifier.create(authenticate)
                .expectErrorMessage("invalid username or password")
                .verify();
        StepVerifier.create(authenticate)
                .expectError(BadCredentialsException.class)
                .verify();

    }


    @Test
    void ShouldReturnMonoWithValidAuthenticationTokenWhenValidAuthenticationTokenIsPassedAsAnArgument() {
        ReactiveAuthenticationProvider provider     = new ReactiveUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
        Mono<Authentication>           authenticate = provider.authenticate(new UsernamePasswordAuthenticationToken("username", "password"));
        StepVerifier.create(authenticate)
                .expectNextCount(1)
                .verifyComplete();
        UserDetails details = org.springframework.security.core.userdetails.User.withUsername("username").password("password").authorities(Collections.emptyList()).build();
        StepVerifier.create(authenticate)
                .expectNext(new UsernamePasswordAuthenticationToken(details,details.getPassword(),details.getAuthorities()))
                .verifyComplete();

    }


}