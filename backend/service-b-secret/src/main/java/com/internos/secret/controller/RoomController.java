package com.internos.secret.controller;

import com.internos.secret.dto.*;
import com.internos.secret.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<CreateRoomResp> createRoom(@Valid @RequestBody CreateRoomReq req) {
        CreateRoomResp resp = roomService.createRoom(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomMeta> getRoom(@PathVariable Long id) {
        RoomMeta meta = roomService.getRoomMeta(id);
        return ResponseEntity.ok(meta);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UpdatedResp> updateRoom(@PathVariable Long id,
                                                  @RequestBody UpdateRoomReq req) {
        UpdatedResp resp = roomService.updateRoom(id, req);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public")
    public ResponseEntity<PublicRoomsResp> getPublicRooms(
            @RequestParam(required = false, defaultValue = "trending") String sort,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor) {
        PublicRoomsResp resp = roomService.getPublicRooms(sort, limit, cursor);
        return ResponseEntity.ok(resp);
    }
}

