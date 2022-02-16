package com.nimvb.app.discord.util;

import com.nimvb.app.discord.domain.User;
import com.nimvb.app.discord.service.RolesProvider;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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
                .withRoles(roles.toArray(new String[0]))
                .build();
    }

    public static final Set<RolesProvider.Role> DEFAULT_ROLES = Set.of(RolesProvider.Role.USER, RolesProvider.Role.ADMIN, RolesProvider.Role.deserialize("ROLE_FAKE"));
    public static final Set<String> DEFAULT_ROLES_SERIALIZED = DEFAULT_ROLES.stream().map(RolesProvider.Role::toString).collect(Collectors.toUnmodifiableSet());

}
