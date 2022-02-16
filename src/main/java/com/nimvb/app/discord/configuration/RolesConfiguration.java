package com.nimvb.app.discord.configuration;

import com.nimvb.app.discord.service.PropertySourceRolesProvider;
import com.nimvb.app.discord.service.RolesProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RolesConfiguration {

    @Bean
    RolesProvider rolesProvider(){
        return new PropertySourceRolesProvider();
    }
}
