package com.nimvb.app.discord.util;

import com.nimvb.app.discord.domain.User;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;

public final class UserBuilder {
    
    private UserBuilder(){}

    public   static User build(String username, String password, String email){
        return build(username, password, email, Collections.emptySet());
    }
    
    public static User build(String username, String password, String email, @NonNull Collection<String> roles){
        return User.builder()
                .withUsername(username)
                .withPassword(password)
                .withEmail(email)
                .build();
    }
    
}
