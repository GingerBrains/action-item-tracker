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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

        when(actionItemRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(actionItem)));

        Page<ActionItemResponse> result = actionItemService.getAllActionItems(PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Review Q3 financials");

        verify(actionItemRepository, times(1)).findAll(any(Pageable.class));
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

    @Test
    void updateActionItem_savesAndReturnsResponse() {
        ActionItem actionItem = new ActionItem();
        actionItem.setId(1L);
        actionItem.setTitle("Original Title");
        actionItem.setStatus(ActionItem.Status.OPEN);
        actionItem.setPriority(ActionItem.Priority.HIGH);
        actionItem.setDueDate(LocalDate.of(2026, 7, 10));

        ActionItem updatedActionItem = new ActionItem();
        updatedActionItem.setId(1L);
        updatedActionItem.setTitle("Updated Title");
        updatedActionItem.setStatus(ActionItem.Status.COMPLETED);
        updatedActionItem.setPriority(ActionItem.Priority.HIGH);
        updatedActionItem.setDueDate(LocalDate.of(2026, 7, 10));

        Meeting meeting = new Meeting();
        meeting.setId(9L);
        meeting.setTitle("Q3 Board Meeting");
        meeting.setStatus(Meeting.Status.SCHEDULED);
        actionItem.setMeeting(meeting);
        updatedActionItem.setMeeting(meeting);

        ActionItemRequest request = ActionItemRequest.builder()
                .title("Updated Title")
                .dueDate(LocalDate.of(2026, 7, 10))
                .status(ActionItem.Status.COMPLETED)
                .priority(ActionItem.Priority.HIGH)
                .meetingId(9L)
                .build();

        when(actionItemRepository.findById(1L)).thenReturn(Optional.of(actionItem));
        when(actionItemRepository.save(any(ActionItem.class))).thenReturn(updatedActionItem);
        when(meetingRepository.findById(9L)).thenReturn(Optional.of(meeting));

        ActionItemResponse result = actionItemService.updateActionItem(1L, request);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        verify(actionItemRepository, times(1)).save(any(ActionItem.class));
        verify(meetingRepository, times(1)).findById(9L);
    }

    @Test
    void updateActionItem_throwsWhenNotFound() {
        ActionItemRequest request = ActionItemRequest.builder()
                .title("Updated Title")
                .dueDate(LocalDate.of(2026, 7, 10))
                .status(ActionItem.Status.COMPLETED)
                .priority(ActionItem.Priority.HIGH)
                .meetingId(9L)
                .build();

        when(actionItemRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actionItemService.updateActionItem(9L, request))
                .isInstanceOf(ResourceNotFoundException.class).
                hasMessageContaining("9");

    }

    @Test
    void deleteActionItem_deletesWhenfound() {
        ActionItem actionItem = new ActionItem();
        actionItem.setId(1L);
        actionItem.setTitle("Original Title");
        actionItem.setStatus(ActionItem.Status.OPEN);
        actionItem.setPriority(ActionItem.Priority.HIGH);
        actionItem.setDueDate(LocalDate.of(2026, 7, 10));


        Meeting meeting = new Meeting();
        meeting.setId(9L);
        meeting.setTitle("Q3 Board Meeting");
        meeting.setStatus(Meeting.Status.SCHEDULED);
        actionItem.setMeeting(meeting);

        when(actionItemRepository.findById(1L)).thenReturn(Optional.of(actionItem));
        doNothing().when(actionItemRepository).delete(any(ActionItem.class));

        actionItemService.deleteActionItem(1L);


        verify(actionItemRepository, times(1)).delete(any(ActionItem.class));
        verify(actionItemRepository, times(1)).findById(1L);
    }

    @Test
    void deleteActionItem_throwsWhenNotFound() {
        when(actionItemRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actionItemService.deleteActionItem(9L))
                .isInstanceOf(ResourceNotFoundException.class).
                hasMessageContaining("9");
    }

}
