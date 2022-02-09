package com.nimvb.app.discord.configuration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(SpringExtension.class)
@Import({SecurityOfPublicResourcesConfiguration.class})
@AutoConfigureWebTestClient(timeout = "PT15M")
@WebFluxTest(excludeFilters = {@ComponentScan.Filter(RestController.class)},excludeAutoConfiguration = {SecurityOfPrivateResourcesConfiguration.class})
class SecurityOfPublicResourcesConfigurationTest {

    @Autowired
    WebTestClient client;

    @Test
    void ShouldProvideSecurityWebFilterChainBeanForPublicResources(){

        new ApplicationContextRunner().withUserConfiguration(SecurityOfPublicResourcesConfiguration.class).run(context -> {
            Assertions.assertThat(context).hasSingleBean(SecurityWebFilterChain.class);
        });
    }

    @Test
    void ShouldReturnProperStatusCodeForAllOptionsRequests(){
        client
                .options()
                .uri("/v3/api-docs/")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void ShouldReturnResponseWithNotFoundAsStatusCodeWhenRequestsAreSentToPublicResources(){
        client
                .get()
                .uri("/v3/api-docs/")
                .exchange()
                .expectStatus()
                .isNotFound();

        client
                .get()
                .uri("/swagger-ui/")
                .exchange()
                .expectStatus()
                .isNotFound();

        client
                .get()
                .uri("/swagger-ui.html")
                .exchange()
                .expectStatus()
                .isNotFound();


        client
                .get()
                .uri("/webjars/swagger-ui/")
                .exchange()
                .expectStatus()
                .isNotFound();

    }



}