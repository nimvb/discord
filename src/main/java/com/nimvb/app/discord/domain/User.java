package com.nimvb.app.discord.domain;


import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


@AllArgsConstructor
@Data
@Document(collection = "users")
@EqualsAndHashCode
public class User {

    @Indexed(unique = true,background = true)
    @NotNull(message = "username is required")
    @NotBlank(message = "invalid username")
    private final String username;
    @NotNull(message = "password is required")
    @NotBlank(message = "invalid password")
    private String password;
    @Indexed(unique = true,background = true)
    @NotNull(message = "email is required")
    @NotBlank(message = "invalid email")
    @Email
    private final String             email;
    @NonNull
    private final Collection<String> roles;
}
