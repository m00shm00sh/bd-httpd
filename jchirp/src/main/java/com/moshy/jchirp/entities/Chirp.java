package com.moshy.jchirp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Chirps")
public class Chirp extends IdTableBase {

    @NotNull
    @Length(max=140)
    private String body;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private User user;

    @Override
    public String toString() {
        return "Chirp{" +
            "id=" + getId() +
            ", createdAt=" + getCreatedAt() +
            ", updatedAt=" + getUpdatedAt() +
            ", body='" + getBody() + '\'' +
            ", userId=" + user.getId() +
            '}';
    }
}
