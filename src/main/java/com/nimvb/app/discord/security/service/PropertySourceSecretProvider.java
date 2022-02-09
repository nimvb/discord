package com.nimvb.app.discord.security.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PropertySourceSecretProvider implements SecretProvider {

    @Value("${spring.security.jwe.secret:#{T(com.nimvb.app.discord.security.service.SecretProvider).DEFAULT_SECRET}}")
    private String secret;


    public String secret(){
        return secret;
    }

}
