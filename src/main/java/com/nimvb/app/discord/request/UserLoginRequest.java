package com.nimvb.app.discord.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@Getter
public class UserLoginRequest {
    @Email(message = "invalid email address")
    @NotBlank
    private final String email;
    @Size(min = 6,max=12,message = "password length should be greater than 6 characters")
    @NotBlank
    private final String password;
}
