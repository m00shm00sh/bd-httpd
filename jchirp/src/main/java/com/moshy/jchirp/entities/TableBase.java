package com.moshy.jchirp.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class TableBase {
    // we can't use @NonNull here because there is no point in forbidding client side null on an unused insert column
    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
