package com.nimvb.app.discord.security.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class PropertySourceSecretProviderTest {


    @Test
    void ShouldReturnDefaultSecretWhenThereIsNoExternalPropertyDefined() {
        new <SecretProvider>ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
                .withBean(null, SecretProvider.class, PropertySourceSecretProvider::new, bd -> {
                })
                .run(context -> {
                    final SecretProvider secretProvider = context.getBean(SecretProvider.class);
                    final String secret = secretProvider.secret();
                    Assertions.assertThat(secret).isNotBlank().isEqualTo(SecretProvider.DEFAULT_SECRET);
                });
    }


    @Test
    void ShouldReturnExternalizedSecretWhenTheCorrespondentExternalPropertyIsDefined() {
        new <SecretProvider>ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
                .withPropertyValues("spring.security.jwe.secret=secretsecret")
                .withBean(null, SecretProvider.class, PropertySourceSecretProvider::new, bd -> {
                })
                .run(context -> {
                    final SecretProvider secretProvider = context.getBean(SecretProvider.class);
                    final String secret = secretProvider.secret();
                    Assertions.assertThat(secret).isNotBlank().isEqualTo("secretsecret");
                });
    }

}