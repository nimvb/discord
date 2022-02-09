package com.nimvb.app.discord.configuration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class ClockConfigurationTest {


    @Test
    void ShouldProvideABeanOfClockType(){
        new ApplicationContextRunner().withUserConfiguration(ClockConfiguration.class).run(context -> {
            Assertions.assertThat(context).hasSingleBean(Clock.class);
        });
    }

}