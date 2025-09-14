package com.moshy.jchirp.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/* we need a UserResponse because User (Entity) uses Entity references instead of raw UUIDs, making the result
 * JSON invalid
 */
@Data
public class UserResponse {
    private UUID id;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
    private String email;
    // use the boxed boolean so lombok generates getIsChirpyRed, setIsChirpyRed
    @JsonProperty("is_chirpy_red")
    private Boolean isChirpyRed;
}
