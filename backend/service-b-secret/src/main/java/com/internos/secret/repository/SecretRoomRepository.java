package com.internos.secret.repository;

import com.internos.secret.entity.SecretRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SecretRoomRepository extends JpaRepository<SecretRoom, Long> {

    @Query("SELECT sr FROM SecretRoom sr WHERE sr.id = :id AND sr.visibility = 'PUBLIC' AND sr.isActive = true")
    Optional<SecretRoom> findPublicById(@Param("id") Long id);

    @Query("SELECT sr FROM SecretRoom sr WHERE sr.id = :id AND (sr.visibility = 'PUBLIC' OR sr.ownerId = :ownerId)")
    Optional<SecretRoom> findByIdForOwner(@Param("id") Long id, @Param("ownerId") Long ownerId);

    @Query("SELECT sr FROM SecretRoom sr WHERE sr.visibility = 'PUBLIC' AND sr.isActive = true ORDER BY sr.createdAt DESC")
    List<SecretRoom> findPublicRoomsOrderByNew(Pageable pageable);

    @Query("SELECT sr FROM SecretRoom sr WHERE sr.ownerId = :ownerId ORDER BY sr.createdAt DESC")
    List<SecretRoom> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT sr FROM SecretRoom sr WHERE sr.visibility = 'PUBLIC' AND sr.isActive = true " +
           "AND (:cursor IS NULL OR sr.createdAt < :cursor) ORDER BY sr.createdAt DESC")
    List<SecretRoom> findPublicRoomsWithCursor(@Param("cursor") Instant cursor, Pageable pageable);
}

