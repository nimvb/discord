package com.nimvb.app.discord.request;

import io.netty.handler.codec.socks.SocksRequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.*;

@AllArgsConstructor
@Getter
public class UserRegistrationRequest {
    @Size(min = 3,message = "username length should be greater than 3")
    @NotBlank(message = "username is required")
    private final String username;
    @Size(min = 6,message = "password length should be greater than 6")
    @NotBlank(message = "password is required")
    private final String password;
    @Email(message = "invalid email address")
    @NotBlank(message = "email is required")
    private final String email;
}
