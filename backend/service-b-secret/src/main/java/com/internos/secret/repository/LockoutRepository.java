package com.internos.secret.repository;

import com.internos.secret.entity.Lockout;
import com.internos.secret.entity.LockoutId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface LockoutRepository extends JpaRepository<Lockout, LockoutId> {

    Optional<Lockout> findByRoomIdAndIpHash(Long roomId, String ipHash);

    @Modifying
    @Transactional
    @Query("DELETE FROM Lockout l WHERE l.until < :now")
    void deleteExpiredLockouts(@Param("now") Instant now);

    @Query("SELECT l FROM Lockout l WHERE l.roomId = :roomId AND l.ipHash = :ipHash AND l.until > :now")
    Optional<Lockout> findActiveLockout(@Param("roomId") Long roomId, @Param("ipHash") String ipHash, @Param("now") Instant now);
}

