package com.internos.secret.repository;

import com.internos.secret.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.roomId = :roomId AND a.createdAt >= :since")
    Long countByRoomIdSince(@Param("roomId") Long roomId, @Param("since") Instant since);

    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.roomId = :roomId AND a.isCorrect = true AND a.createdAt >= :since")
    Long countCorrectByRoomIdSince(@Param("roomId") Long roomId, @Param("since") Instant since);

    @Query("SELECT a FROM Attempt a WHERE a.roomId = :roomId ORDER BY a.createdAt DESC")
    List<Attempt> findByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.roomId = :roomId AND a.ipHash = :ipHash " +
           "AND a.isCorrect = false AND a.createdAt >= :since")
    Long countFailedAttemptsSince(@Param("roomId") Long roomId, @Param("ipHash") String ipHash, @Param("since") Instant since);
}

