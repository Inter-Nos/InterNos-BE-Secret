package com.internos.secret.controller;

import com.internos.secret.dto.TrendList;
import com.internos.secret.repository.AttemptRepository;
import com.internos.secret.repository.SecretRoomRepository;
import com.internos.secret.entity.SecretRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rank")
@RequiredArgsConstructor
public class RankController {

    private final SecretRoomRepository roomRepository;
    private final AttemptRepository attemptRepository;

    @GetMapping("/trending")
    public ResponseEntity<TrendList> getTrending(@RequestParam(required = false, defaultValue = "50") Integer limit) {
        // Get active public rooms
        Pageable pageable = PageRequest.of(0, Math.min(limit, 100));
        List<SecretRoom> rooms = roomRepository.findPublicRoomsOrderByNew(pageable);

        Instant oneHourAgo = Instant.now().minusSeconds(3600);

        List<TrendList.TrendItem> items = rooms.stream()
                .map(room -> {
                    Long attempts1h = attemptRepository.countByRoomIdSince(room.getId(), oneHourAgo);
                    Long correct1h = attemptRepository.countCorrectByRoomIdSince(room.getId(), oneHourAgo);
                    Double solveRate1h = attempts1h > 0 
                        ? (correct1h.doubleValue() / attempts1h.doubleValue()) 
                        : 0.0;
                    
                    // Trending score: attempts * solve rate
                    Double trendScore = attempts1h * solveRate1h;

                    return TrendList.TrendItem.builder()
                            .roomId(room.getId())
                            .trendScore(trendScore)
                            .attempts1h(attempts1h.intValue())
                            .solveRate1h(solveRate1h)
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getTrendScore(), a.getTrendScore())) // Descending
                .limit(limit)
                .collect(Collectors.toList());

        TrendList response = TrendList.builder()
                .items(items)
                .build();

        return ResponseEntity.ok(response);
    }
}

