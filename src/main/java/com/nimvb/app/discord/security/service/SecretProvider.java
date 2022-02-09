package com.nimvb.app.discord.security.service;

public interface SecretProvider {

    String DEFAULT_SECRET = "secret";

    /**
     * Retrieve the required secret
     * @return secret
     */
    String secret();
}
