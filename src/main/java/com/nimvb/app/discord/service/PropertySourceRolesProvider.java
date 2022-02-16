package com.nimvb.app.discord.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;


public class PropertySourceRolesProvider implements RolesProvider{


    @Value("${spring.security.roles.default:#{T(com.nimvb.app.discord.service.RolesProvider).DEFAULT_ROLES}}")
    String[] roles;

    @Override
    public Set<Role> provide() {
        return Collections.unmodifiableSet(Arrays.stream(roles).map(Role::deserialize).collect(Collectors.toUnmodifiableSet()));
    }
}
