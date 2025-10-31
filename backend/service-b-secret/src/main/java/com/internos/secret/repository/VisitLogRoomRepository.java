package com.internos.secret.repository;

import com.internos.secret.entity.VisitLogRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitLogRoomRepository extends JpaRepository<VisitLogRoom, Long> {
}

