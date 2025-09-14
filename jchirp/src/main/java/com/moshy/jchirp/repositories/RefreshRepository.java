package com.moshy.jchirp.repositories;

import com.moshy.jchirp.entities.Refresh;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshRepository extends JpaRepository<Refresh, String> {
    Optional<Refresh> findByTokenAndExpiresAtGreaterThanAndRevokedAtIsNull(String token, LocalDateTime expiresAt);

    @Modifying
    @Query("update Refresh r set r.revokedAt = ?2 where r.token = ?1")
    int revokeToken(String token, LocalDateTime revokedAt);
}
