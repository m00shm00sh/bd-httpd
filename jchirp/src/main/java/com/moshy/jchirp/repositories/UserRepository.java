package com.moshy.jchirp.repositories;

import com.moshy.jchirp.entities.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, Refreshable<User> {
    Optional<User> findByEmail(String email);

    @Modifying
    @Query("update User u set u.isChirpyRed = true where u.id = ?1")
    int upgradeUserToRed(UUID id);
}
