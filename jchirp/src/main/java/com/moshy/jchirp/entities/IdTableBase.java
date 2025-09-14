package com.moshy.jchirp.entities;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class IdTableBase extends TableBase {
    @Id
    @NotNull
    private UUID id;

    protected IdTableBase() {
        setId(uuidGenerator.generate());
    }

    protected static final NoArgGenerator uuidGenerator = Generators.timeBasedEpochRandomGenerator();
}
