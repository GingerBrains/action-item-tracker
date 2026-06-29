package com.laurel.actiontracker.controller;

import com.laurel.actiontracker.dto.request.MeetingRequest;
import com.laurel.actiontracker.dto.response.MeetingResponse;
import com.laurel.actiontracker.service.MeetingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService){
        this.meetingService = meetingService;
    }

    @GetMapping
    public ResponseEntity<Page<MeetingResponse>> getAllMeetings(
            @PageableDefault(size = 20, sort = "meetingDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(meetingService.getAllMeetings(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getMeetingById(@PathVariable Long id){
        return ResponseEntity.ok((meetingService.getMeetingById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MeetingResponse> createMeeting(@Valid @RequestBody MeetingRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingService.createMeeting(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MeetingResponse> updateMeeting(@PathVariable Long id, @Valid @RequestBody MeetingRequest request){
        return ResponseEntity.ok(meetingService.updateMeeting(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long id){
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }
}
