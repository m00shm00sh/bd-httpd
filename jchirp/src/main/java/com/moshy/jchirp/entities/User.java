package com.moshy.jchirp.entities;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@ToString
@Table(name = "Users")
public class User extends IdTableBase {
    @NotNull
    private String email;

    @Column(name = "pass")
    @NotNull
    private String password;

    @NotNull
    private boolean isChirpyRed;

    public User() {
        setChirpyRed(false);
    }
}
