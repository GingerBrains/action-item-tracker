package com.laurel.actiontracker.dto.response;

import com.laurel.actiontracker.entity.Meeting;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
public class MeetingResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate meetingDate;
    private Meeting.Status status;
    private Instant createdAt;
    private Instant updatedAt;

    public static MeetingResponse from(Meeting meeting){
        MeetingResponse dto = new MeetingResponse();
        dto.id = meeting.getId();
        dto.title = meeting.getTitle();
        dto.description = meeting.getDescription();
        dto.meetingDate = meeting.getMeetingDate();
        dto.status = meeting.getStatus();
        dto.createdAt = meeting.getCreatedAt();
        dto.updatedAt = meeting.getUpdatedAt();
        return dto;
    }
}
