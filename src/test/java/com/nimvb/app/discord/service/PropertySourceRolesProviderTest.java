package com.nimvb.app.discord.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
class PropertySourceRolesProviderTest {

    private static final String[] DEFAULT_ROLES = {
            "ROLE_USER",
            "ROLE_ADMIN",
            "ROLE_FAKE"
    };

    private static final String PROPERTY_PATH = "spring.security.roles.default";
    private static final String PROPERTY_WITH_EXTERNALIZED_DEFAULT_ROLES = PROPERTY_PATH +"=" + Arrays.stream(DEFAULT_ROLES).reduce((s, s2) -> s +","+s2).orElse("");
    private static final String PROPERTY_WITH_EMPTY_DEFAULT_ROLES = PROPERTY_PATH +"=" + "";


    @Test
    void ShouldReturnDefaultRolesWhenThereIsNoExternalPropertyDefined() {
        new <RolesProvider>ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
                .withBean(null, RolesProvider.class, PropertySourceRolesProvider::new, bd -> {
                })
                .run(context -> {
                    final RolesProvider rolesProvider = context.getBean(RolesProvider.class);
                    final Set<RolesProvider.Role> roles = rolesProvider.provide();
                    Assertions.assertThat(roles)
                            .isNotNull()
                            .isNotEmpty()
                            .isUnmodifiable()
                            .hasSize(RolesProvider.DEFAULT_ROLES.length)
                            .containsAll(Arrays.stream(RolesProvider.DEFAULT_ROLES).map(RolesProvider.Role::deserialize).collect(Collectors.toList()));
                });
    }


    @Test
    void ShouldReturnExternalizedDefaultRolesWhenTheCorrespondentExternalPropertyIsDefined() {

        new <RolesProvider>ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
                .withPropertyValues(PROPERTY_WITH_EXTERNALIZED_DEFAULT_ROLES)
                .withBean(null, RolesProvider.class, PropertySourceRolesProvider::new, bd -> {
                })
                .run(context -> {

                    final RolesProvider rolesProvider = context.getBean(RolesProvider.class);
                    final Set<RolesProvider.Role> roles = rolesProvider.provide();
                    Assertions.assertThat(roles)
                            .isNotNull()
                            .isNotEmpty()
                            .isUnmodifiable()
                            .hasSize(DEFAULT_ROLES.length)
                            .containsAll(Arrays.stream(RolesProvider.DEFAULT_ROLES).map(RolesProvider.Role::deserialize).collect(Collectors.toList()));
                });
    }


    @Test
    void ShouldReturnEmptyRolesWhenTheCorrespondentExternalPropertyIsDefinedToBeEmpty() {

        new <RolesProvider>ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
                .withPropertyValues(PROPERTY_WITH_EMPTY_DEFAULT_ROLES)
                .withBean(null, RolesProvider.class, PropertySourceRolesProvider::new, bd -> {
                })
                .run(context -> {

                    final RolesProvider rolesProvider = context.getBean(RolesProvider.class);
                    final Set<RolesProvider.Role> roles = rolesProvider.provide();
                    Assertions.assertThat(roles)
                            .isNotNull()
                            .isEmpty();
                });
    }


}