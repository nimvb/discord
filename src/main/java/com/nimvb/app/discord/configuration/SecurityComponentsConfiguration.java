package com.nimvb.app.discord.configuration;

import com.nimvb.app.discord.security.manager.CustomReactiveAuthenticationManager;
import com.nimvb.app.discord.security.provider.ReactiveBearerTokenAuthenticationProvider;
import com.nimvb.app.discord.security.provider.ReactiveUsernamePasswordAuthenticationProvider;
import com.nimvb.app.discord.security.service.SecretProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SecurityComponentsConfiguration {


    private final ReactiveUserDetailsService reactiveUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final SecretProvider secretProvider;

    @Bean
    ReactiveAuthenticationManager reactiveAuthenticationManager(){
        final CustomReactiveAuthenticationManager.Builder builder = CustomReactiveAuthenticationManager.builder();
        builder.add(new ReactiveUsernamePasswordAuthenticationProvider(reactiveUserDetailsService,passwordEncoder));
        builder.add(new ReactiveBearerTokenAuthenticationProvider(secretProvider));
        return builder.build();
    }
}
