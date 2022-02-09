package com.nimvb.app.discord.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessTokenTest {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void ShouldThrownAnExceptionWhenAnyRequiredParameterIsNULL(){
        Assertions.assertThatThrownBy(() -> {
            AccessToken accessToken = new AccessToken(null,null,0,null,null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    void ShouldHasTheRequiredJsonFieldsWhenSerializedToJson() throws JsonProcessingException {
        AccessToken accessToken = new AccessToken("token","type",0,"refresh_token","scope");
        final String token = MAPPER.writeValueAsString(accessToken);
        final DocumentContext context = JsonPath.parse(token);
        context.read("$.access_token", predicateContext -> {
            return predicateContext.item().equals("token");
        });
        context.read("$.refresh_token",predicateContext -> {
            return predicateContext.item().equals("refresh_token");
        });
        context.read("$.expirationTime",predicateContext -> {
            return predicateContext.item().equals(0);
        });
        context.read("$.type",predicateContext -> {
            return predicateContext.item().equals("type");
        });
        context.read("$.scope",predicateContext -> {
            return predicateContext.item().equals("scope");
        });

    }

}