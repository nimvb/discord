package com.nimvb.app.discord.security.resource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;


@EqualsAndHashCode(callSuper = true)
public class BearerTokenAuthenticationToken extends AbstractAuthenticationToken {

    @Getter
    private final String token;

    public BearerTokenAuthenticationToken(String token) throws IllegalArgumentException{
        super(Collections.emptyList());
        Assert.hasText(token, "token cannot be empty");
        this.token = token;
        setAuthenticated(false);
    }

    public BearerTokenAuthenticationToken(String token, String username, Collection<? extends GrantedAuthority> authorities) throws IllegalArgumentException{
        super(authorities);
        Assert.hasText(token, "token cannot be empty");
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }
}
