package com.nimvb.app.discord.configuration;

import com.nimvb.app.discord.security.service.PropertySourceSecretProvider;
import com.nimvb.app.discord.security.service.SecretProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class EncodingConfiguration {

    @Bean
    PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecretProvider secretProvider(){
        return new PropertySourceSecretProvider();
    }
}
