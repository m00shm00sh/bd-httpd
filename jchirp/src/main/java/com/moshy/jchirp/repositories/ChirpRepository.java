package com.moshy.jchirp.repositories;

import com.moshy.jchirp.entities.Chirp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChirpRepository extends JpaRepository<Chirp, UUID>, Refreshable<Chirp> {
    List<Chirp> findByUserIdOrderByCreatedAt(UUID userId);
    List<Chirp> findByOrderByCreatedAt();
}
