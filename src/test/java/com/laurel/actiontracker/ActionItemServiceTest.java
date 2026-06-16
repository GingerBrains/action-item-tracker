package com.laurel.actiontracker;

import com.laurel.actiontracker.dto.request.ActionItemRequest;
import com.laurel.actiontracker.dto.response.ActionItemResponse;
import com.laurel.actiontracker.entity.ActionItem;
import com.laurel.actiontracker.entity.Meeting;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.repository.ActionItemRepository;
import com.laurel.actiontracker.repository.MeetingRepository;
import com.laurel.actiontracker.repository.UserRepository;
import com.laurel.actiontracker.service.impl.ActionItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActionItemServiceTest {

    @Mock
    private ActionItemRepository actionItemRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @InjectMocks
    private ActionItemServiceImpl actionItemService;

    @Test
    void getAllActionItems_returnsMappedResponse() {
        ActionItem actionItem = new ActionItem();
        actionItem.setId(1L);
        actionItem.setTitle("Review Q3 financials");
        actionItem.setStatus(ActionItem.Status.OPEN);
        actionItem.setPriority(ActionItem.Priority.HIGH);
        actionItem.setDueDate(LocalDate.of(2026, 7, 10));

        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setTitle("Q3 Board Meeting");
        meeting.setStatus(Meeting.Status.SCHEDULED);
        actionItem.setMeeting(meeting);

        when(actionItemRepository.findAll()).thenReturn(List.of(actionItem));

        List<ActionItemResponse> result = actionItemService.getAllActionItems();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Review Q3 financials");

        verify(actionItemRepository, times(1)).findAll();
    }

    @Test
    void getActionItemById_returnsCorrectResponse() {
        ActionItem actionItem = new ActionItem();
        actionItem.setId(1L);
        actionItem.setTitle("Review Q3 financials");
        actionItem.setStatus(ActionItem.Status.OPEN);
        actionItem.setPriority(ActionItem.Priority.HIGH);
        actionItem.setDueDate(LocalDate.of(2026, 7, 10));

        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setTitle("Q3 Board Meeting");
        meeting.setStatus(Meeting.Status.SCHEDULED);
        actionItem.setMeeting(meeting);

        when(actionItemRepository.findById(1L)).thenReturn(Optional.of(actionItem));

        ActionItemResponse result = actionItemService.getActionItemById(1L);

        assertThat(result.getId()).isEqualTo(1L);

        verify(actionItemRepository, times(1)).findById(1L);
    }

    @Test
    void getActionItemById_throwsWhenNotFound(){
        when(actionItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actionItemService.getActionItemById(99L))
                .isInstanceOf(ResourceNotFoundException.class).
                hasMessageContaining("99");
    }

    @Test
    void createActionItem_savesAndReturnsResponse() {
        ActionItem actionItem = new ActionItem();
        actionItem.setId(1L);
        actionItem.setTitle("Review Q3 financials");
        actionItem.setStatus(ActionItem.Status.OPEN);
        actionItem.setPriority(ActionItem.Priority.HIGH);
        actionItem.setDueDate(LocalDate.of(2026, 7, 10));

        Meeting meeting = new Meeting();
        meeting.setId(9L);
        meeting.setTitle("Q3 Board Meeting");
        meeting.setStatus(Meeting.Status.SCHEDULED);
        actionItem.setMeeting(meeting);

        ActionItemRequest request = ActionItemRequest.builder()
                .title("Review Q3 financials")
                .dueDate(LocalDate.of(2026, 7, 10))
                .status(ActionItem.Status.OPEN)
                .priority(ActionItem.Priority.HIGH)
                .meetingId(9L)
                .build();


        when(actionItemRepository.save(any(ActionItem.class))).thenReturn(actionItem);
        when(meetingRepository.findById(9L)).thenReturn(Optional.of(meeting));
        ActionItemResponse result = actionItemService.createActionItem(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(actionItemRepository, times(1)).save(any(ActionItem.class));
        verify(meetingRepository, times(1)).findById(9L);
    }
}
