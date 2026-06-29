package com.laurel.actiontracker.dto.response;

import com.laurel.actiontracker.entity.ActionItem;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
public class ActionItemResponse {
    private Long meetingId;
    private Long id;
    private String title;
    private String description;
    private ActionItem.Status status;
    private ActionItem.Priority priority;
    private LocalDate dueDate;
    private Long assigneeId;
    private String assigneeName;
    private Instant createdAt;
    private Instant updatedAt;

    public static ActionItemResponse from(ActionItem actionItem) {
        ActionItemResponse dto = new ActionItemResponse();
        dto.meetingId = actionItem.getMeeting().getId();
        dto.id = actionItem.getId();
        dto.title = actionItem.getTitle();
        dto.description = actionItem.getDescription();
        dto.status = actionItem.getStatus();
        dto.priority = actionItem.getPriority();
        dto.dueDate = actionItem.getDueDate();
        if (actionItem.getAssignee() != null) {
            dto.assigneeId = actionItem.getAssignee().getId();
            dto.assigneeName = actionItem.getAssignee().getFullName();
        }
        dto.createdAt = actionItem.getCreatedAt();
        dto.updatedAt = actionItem.getUpdatedAt();
        return dto;

    }
}
