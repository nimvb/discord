package com.nimvb.app.discord.configuration;

import com.nimvb.app.discord.service.RolesProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class RolesConfigurationTest {

    @Test
    void ShouldProvideABeanForRolesProvider(){
        new ApplicationContextRunner()
                .withUserConfiguration(RolesConfiguration.class)
                .run(context -> {
                    Assertions.assertThat(context).hasSingleBean(RolesProvider.class);
                });
    }

}