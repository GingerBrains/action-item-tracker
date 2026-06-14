package com.laurel.actiontracker.dto.request;

import com.laurel.actiontracker.entity.Meeting;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MeetingRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Meeting date is required")
    private LocalDate meetingDate;

    @NotNull(message = "Status is required")
    private Meeting.Status status;

    public Meeting toEntity() {
        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setMeetingDate(meetingDate);
        meeting.setStatus(status);
        return meeting;
    }
}