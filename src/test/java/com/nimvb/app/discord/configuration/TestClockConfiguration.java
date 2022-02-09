package com.nimvb.app.discord.configuration;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Test configuration to provide a fixed {@link Clock} object
 */
@TestConfiguration
public class TestClockConfiguration {

    @Bean
    Clock clock(){
        //2021-12-20T11:33:20Z
        return Clock.fixed(LocalDateTime.of(2021,12,20,11,33,20,0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
    }
}
