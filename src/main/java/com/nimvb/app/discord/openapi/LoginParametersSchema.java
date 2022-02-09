package com.nimvb.app.discord.openapi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class LoginParametersSchema {


    @Schema(name = "username",required = true)
    private final String username;
    @Schema(name = "password",required = true)
    private final String password;
}
