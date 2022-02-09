package com.nimvb.app.discord.controller;

import com.nimvb.app.discord.model.AccessToken;
import com.nimvb.app.discord.openapi.LoginParametersSchema;
import com.nimvb.app.discord.service.AccessTokenConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/authenticate")
@RequiredArgsConstructor
public class LoginController {

    private final AccessTokenConverter<UsernamePasswordAuthenticationToken> converter;

    @Operation(
            requestBody = @RequestBody(content = {@Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = LoginParametersSchema.class))})
    )
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<AccessToken> authenticate(/*@RequestParam Map<String,String> parameters,*/ @AuthenticationPrincipal Authentication authentication) {
        return converter.convert(((UsernamePasswordAuthenticationToken) authentication));
    }
}
