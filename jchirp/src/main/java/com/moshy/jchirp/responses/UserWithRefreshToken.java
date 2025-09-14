package com.moshy.jchirp.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserWithRefreshToken {
    private UUID id;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
    private String email;
    private String token;
    @JsonProperty("refresh_token")
    private String refreshToken;
    // use the boxed boolean so lombok generates getIsChirpyRed, setIsChirpyRed
    @JsonProperty("is_chirpy_red")
    private Boolean isChirpyRed;
}
