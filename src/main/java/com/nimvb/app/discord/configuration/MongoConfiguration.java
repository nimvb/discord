package com.nimvb.app.discord.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;

@Configuration
public class MongoConfiguration {
    @Bean
    ValidatingMongoEventListener validatingMongoEventListener(LocalValidatorFactoryBean validator){
        return new ValidatingMongoEventListener(validator);
    }
}
