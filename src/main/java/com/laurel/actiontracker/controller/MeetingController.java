package com.laurel.actiontracker.controller;

import com.laurel.actiontracker.dto.request.MeetingRequest;
import com.laurel.actiontracker.dto.response.MeetingResponse;
import com.laurel.actiontracker.service.MeetingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService){
        this.meetingService = meetingService;
    }

    @GetMapping
    public ResponseEntity<List<MeetingResponse>> getAllMeetings() {
        return ResponseEntity.ok(meetingService.getAllMeetings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getMeetingById(@PathVariable Long id){
        return ResponseEntity.ok((meetingService.getMeetingById(id)));
    }

    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(@Valid @RequestBody MeetingRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingService.createMeeting(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MeetingResponse> updateMeeting(@PathVariable Long id, @Valid @RequestBody MeetingRequest request){
        return ResponseEntity.ok(meetingService.updateMeeting(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long id){
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }
}
