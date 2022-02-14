package com.nimvb.app.discord.security.manager;

import com.nimvb.app.discord.domain.User;
import com.nimvb.app.discord.security.provider.ReactiveAuthenticationProvider;
import com.nimvb.app.discord.util.UserBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
class CustomReactiveAuthenticationManagerTest {


    private static Map<String, User> USERS =new HashMap<>();

    @Mock
    ReactiveAuthenticationProvider usernamePasswordProvider;

    @Mock
    ReactiveAuthenticationProvider testingProvider;

    @BeforeAll
    static void initUsers(){
        USERS = new HashMap<>();
        USERS.put("username", UserBuilder.build("username","password","email@email.com"));
        USERS.put("testuser",UserBuilder.build("testuser","password","email@email.com"));
        USERS = Collections.unmodifiableMap(USERS);
    }


    @BeforeEach
    void init(){
        Mockito.when(usernamePasswordProvider.supports(ArgumentMatchers.isNull())).thenAnswer(invocation -> Mono.just(false));
        Mockito.when(usernamePasswordProvider.supports(ArgumentMatchers.isNotNull())).thenAnswer(invocation -> {
            final Class<?> argument = invocation.getArgument(0);
            if(UsernamePasswordAuthenticationToken.class.isAssignableFrom(argument)){
                return Mono.just(true);
            }
            return Mono.just(false);
        });
        Mockito.when(usernamePasswordProvider.authenticate(ArgumentMatchers.isNotNull())).thenAnswer(invocation -> {
            final Authentication argument = invocation.getArgument(0);
            if(!UsernamePasswordAuthenticationToken.class.isAssignableFrom(argument.getClass())){
                return Mono.error(new BadCredentialsException("token is not valid"));
            }
            return Mono.just(argument)
                    .cast(UsernamePasswordAuthenticationToken.class)
                    .map(token -> {
                        final String username = token.getName();
                        if(!USERS.containsKey(username)){
                            throw new RuntimeException();
                        }
                        final User user = USERS.get(username);
                        String credentials = (String) token.getCredentials();
                        if(!user.getPassword().equals(credentials)){
                            throw new RuntimeException();
                        }
                        return new UsernamePasswordAuthenticationToken(username,credentials, Collections.emptyList());
                    })
                    .onErrorResume(throwable -> Mono.error(new BadCredentialsException("invalid token")));
        });

        Mockito.when(testingProvider.supports(ArgumentMatchers.isNull())).thenAnswer(invocation -> Mono.just(false));
        Mockito.when(testingProvider.supports(ArgumentMatchers.isNotNull())).thenAnswer(invocation -> {
            final Class<?> argument = invocation.getArgument(0);
            if(TestingAuthenticationToken.class.isAssignableFrom(argument)){
                return Mono.just(true);
            }
            return Mono.just(false);
        });
        Mockito.when(testingProvider.authenticate(ArgumentMatchers.isNotNull())).thenAnswer(invocation -> {
            final Authentication argument = invocation.getArgument(0);
            if(!TestingAuthenticationToken.class.isAssignableFrom(argument.getClass())){
                return Mono.error(new BadCredentialsException("token is not valid"));
            }
            return Mono.just(argument)
                    .cast(TestingAuthenticationToken.class)
                    .map(token -> {
                        final String username = token.getName();
                        if(!USERS.containsKey(username)){
                            throw new RuntimeException();
                        }
                        final User user = USERS.get(username);
                        String credentials = (String) token.getCredentials();
                        if(!user.getPassword().equals(credentials)){
                            throw new RuntimeException();
                        }
                        return new TestingAuthenticationToken(username,credentials, Collections.emptyList());
                    })
                    .onErrorResume(throwable -> Mono.error(new BadCredentialsException("invalid token")));
        });
    }

    @Test
    void ShouldThrownAnExceptionWhenNULLConstrcutorArgumentIsProvided(){
        Assertions.assertThatThrownBy(() -> {
            new CustomReactiveAuthenticationManager(null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    void ShouldReturnProperBuilderWhenTheBuilderMethodIsInvoked(){
        Assertions.assertThat(CustomReactiveAuthenticationManager.builder()).isNotNull()
                        .isInstanceOf(CustomReactiveAuthenticationManager.Builder.class);
    }

    @Test
    void ShouldThrownAnExceptionWhenNULLProviderIsPassedForBuilder(){
        Assertions.assertThatThrownBy(() -> {
            CustomReactiveAuthenticationManager.builder().add(null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    void ShouldReturnMonoWithErrorWhenTheAuthenticationTokenIsNULL(){
        final ReactiveAuthenticationManager withoutProvider = CustomReactiveAuthenticationManager.builder().build();
        final ReactiveAuthenticationManager withProvider = CustomReactiveAuthenticationManager.builder().add(testingProvider).build();
        final Mono<Authentication> authenticateOfWithoutProvider = withoutProvider.authenticate(null);
        final Mono<Authentication> authenticateOfWithProvider = withProvider.authenticate(null);
        StepVerifier.create(authenticateOfWithoutProvider).expectError().verify();
        StepVerifier.create(authenticateOfWithoutProvider).expectError(BadCredentialsException.class).verify();
        StepVerifier.create(authenticateOfWithProvider).expectError().verify();
        StepVerifier.create(authenticateOfWithProvider).expectError(BadCredentialsException.class).verify();
    }

    @Test
    void ShouldReturnEmptyMonoWhenNoProviderIsDefined(){
        final ReactiveAuthenticationManager build = CustomReactiveAuthenticationManager.builder().build();
        final Mono<Authentication> authenticate = build.authenticate(new RunAsUserToken("","","",Collections.emptyList(),null));
        StepVerifier.create(authenticate).expectNextCount(0).verifyComplete();
    }

    @Test
    void ShouldReturnEmptyMonoWhenNoProviderSupportsTheTokenType(){
        final ReactiveAuthenticationManager build = CustomReactiveAuthenticationManager.builder()
                .add(testingProvider)
                .add(usernamePasswordProvider)
                .build();
        final Mono<Authentication> authenticate = build.authenticate(new RunAsUserToken("","","",Collections.emptyList(),null));
        StepVerifier.create(authenticate).expectNextCount(0).verifyComplete();
    }

    @Test
    void ShouldReturnMonoWithErrorWhenNoProviderIsCapableToAuthenticateTheSupportedToken(){
        final ReactiveAuthenticationManager build = CustomReactiveAuthenticationManager.builder()
                .add(testingProvider)
                .add(usernamePasswordProvider)
                .build();
        Mono<Authentication> authenticate = build.authenticate(new UsernamePasswordAuthenticationToken("u","p"));
        StepVerifier.create(authenticate).expectError().verify();
        StepVerifier.create(authenticate).expectError(BadCredentialsException.class).verify();
        StepVerifier.create(authenticate).expectErrorMessage("invalid credentials").verify();

        authenticate = build.authenticate(new TestingAuthenticationToken("u","p"));
        StepVerifier.create(authenticate).expectError().verify();
        StepVerifier.create(authenticate).expectError(BadCredentialsException.class).verify();
        StepVerifier.create(authenticate).expectErrorMessage("invalid credentials").verify();
    }


    @Test
    void ShouldReturnMonoWithAuthenticationTokenWhenAProviderIsCapableToAuthenticateTheSupportedToken(){
        final ReactiveAuthenticationManager build = CustomReactiveAuthenticationManager.builder()
                .add(testingProvider)
                .add(usernamePasswordProvider)
                .build();
        Mono<Authentication> authenticate = build.authenticate(new UsernamePasswordAuthenticationToken("username","password"));
        StepVerifier.create(authenticate).expectNextCount(1).verifyComplete();
        StepVerifier.create(authenticate).expectNext(new UsernamePasswordAuthenticationToken("username","password",Collections.emptyList())).verifyComplete();


        authenticate = build.authenticate(new TestingAuthenticationToken("username","password"));
        StepVerifier.create(authenticate).expectNextCount(1).verifyComplete();
        StepVerifier.create(authenticate).expectNextMatches(authentication -> authentication instanceof TestingAuthenticationToken).verifyComplete();
        StepVerifier.create(authenticate).expectNext(new TestingAuthenticationToken("username","password",Collections.emptyList())).verifyComplete();
    }

}