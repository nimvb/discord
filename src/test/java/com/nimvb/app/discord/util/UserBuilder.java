package com.nimvb.app.discord.util;

import com.nimvb.app.discord.domain.User;

public final class UserBuilder {
    
    private UserBuilder(){}

    public   static User build(String username, String password, String email){
        return User.builder()
                .withUsername(username)
                .withPassword(password)
                .withEmail(email)
                .build();
    }
}
