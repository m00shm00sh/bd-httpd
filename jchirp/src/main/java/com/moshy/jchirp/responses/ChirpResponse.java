package com.moshy.jchirp.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ChirpResponse {

    private UUID id;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    private String body;

    @JsonProperty("user_id")
    private UUID userId;
}
