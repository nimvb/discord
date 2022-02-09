package com.nimvb.app.discord.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AccessToken {
    @JsonProperty("access_token")
    @NonNull private final String accessToken;
    @NonNull private final String type;
    private final long expirationTime;
    @JsonProperty("refresh_token")
    @NonNull private final String refreshToken;
    @NonNull private final String scope;
}
