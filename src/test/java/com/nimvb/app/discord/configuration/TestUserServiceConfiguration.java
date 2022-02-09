package com.nimvb.app.discord.configuration;

import com.nimvb.app.discord.repository.UserRepository;
import com.nimvb.app.discord.service.UserService;
import com.nimvb.app.discord.service.UserServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestUserServiceConfiguration {

    @Bean
    UserService userService(PasswordEncoder passwordEncoder, UserRepository userRepository){
        return new UserServiceImpl(userRepository, passwordEncoder);
    }
}
