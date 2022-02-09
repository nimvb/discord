package com.nimvb.app.discord.configuration;

import com.nimvb.app.discord.security.service.SecretProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class SecurityComponentsConfigurationTest {

    @MockBean
    SecretProvider secretProvider;

    @MockBean
    ReactiveUserDetailsService reactiveUserDetailsService;

    @MockBean
    PasswordEncoder passwordEncoder;


    @Test
    void ShouldProvideABeanForReactiveAuthenticationManager(){
        new ApplicationContextRunner()
                .withBean(SecretProvider.class, () -> secretProvider,bd -> {})
                .withBean(ReactiveUserDetailsService.class, () -> reactiveUserDetailsService,bd -> {})
                .withBean(PasswordEncoder.class, () -> passwordEncoder,bd -> {})
                .withUserConfiguration(SecurityComponentsConfiguration.class)
                .run(context -> {
            Assertions.assertThat(context).hasSingleBean(SecretProvider.class);
            Assertions.assertThat(context).hasSingleBean(ReactiveUserDetailsService.class);
            Assertions.assertThat(context).hasSingleBean(ReactiveAuthenticationManager.class);
        });
    }


}