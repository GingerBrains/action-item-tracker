package com.laurel.actiontracker.dto.request;

import com.laurel.actiontracker.entity.ActionItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ActionItemRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Status is required")
    private ActionItem.Status status;

    private ActionItem.Priority priority;

    @NotNull(message = "Meeting ID is required")
    private Long meetingId;

    private Long assigneeId;

    public ActionItem toEntity(){
        ActionItem actionItem = new ActionItem();
        actionItem.setTitle(title);
        actionItem.setDescription(description);
        actionItem.setStatus(status);
        actionItem.setPriority(priority);
        actionItem.setDueDate(dueDate);
        return actionItem;
    }
}
