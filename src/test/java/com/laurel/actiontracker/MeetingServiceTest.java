package com.laurel.actiontracker;

import com.laurel.actiontracker.dto.request.MeetingRequest;
import com.laurel.actiontracker.dto.response.MeetingResponse;
import com.laurel.actiontracker.entity.Meeting;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.repository.MeetingRepository;
import com.laurel.actiontracker.service.impl.MeetingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @InjectMocks
    private MeetingServiceImpl meetingService;

    @Test
    void getAllMeetings_returnsMappedResponses() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setTitle("Q1 2026 Board Meeting");
        meeting.setMeetingDate(LocalDate.of(2026, 4, 1));
        meeting.setStatus(Meeting.Status.SCHEDULED);

        when(meetingRepository.findAll()).thenReturn(List.of(meeting));

        List<MeetingResponse> result = meetingService.getAllMeetings();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Q1 2026 Board Meeting");
    }

    @Test
    void getMeetingById_returnsCorrectResponse() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setTitle("New Meeting");
        meeting.setMeetingDate(LocalDate.of(2026, 8, 1));
        meeting.setStatus(Meeting.Status.SCHEDULED);

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        MeetingResponse result = meetingService.getMeetingById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Meeting");

        verify(meetingRepository, times(1)).findById(1L);

    }

    @Test
    void getMeetingById_throwsWhenNotFound(){
        when(meetingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.getMeetingById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createMeeting_savesAndReturnsResponse() {
        MeetingRequest request = MeetingRequest.builder()
                .title("New Meeting")
                .meetingDate(LocalDate.of(2026, 8, 1))
                .status(Meeting.Status.SCHEDULED)
                .build();

        Meeting saved = new Meeting();
        saved.setId(1L);
        saved.setTitle("New Meeting");
        saved.setMeetingDate(LocalDate.of(2026, 8, 1));
        saved.setStatus(Meeting.Status.SCHEDULED);

        when(meetingRepository.save(any(Meeting.class))).thenReturn(saved);

        MeetingResponse result = meetingService.createMeeting(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(meetingRepository, times(1)).save(any(Meeting.class));
    }

    @Test
    void updateMeeting_savesAndReturnsResponse() {
        Meeting existing = new Meeting();
        existing.setId(1L);
        existing.setTitle("Old Title");
        existing.setStatus(Meeting.Status.SCHEDULED);

        Meeting updated = new Meeting();
        updated.setId(1L);
        updated.setTitle("New Updated Meeting");
        updated.setStatus(Meeting.Status.COMPLETED);

        MeetingRequest request = MeetingRequest.builder()
                .title("New Updated Meeting")
                .meetingDate(LocalDate.of(2026, 8, 1))
                .status(Meeting.Status.COMPLETED)
                .build();

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(updated);

        MeetingResponse result = meetingService.updateMeeting(1L, request);

        assertThat(result.getTitle()).isEqualTo("New Updated Meeting");
        assertThat(result.getStatus()).isEqualTo(Meeting.Status.COMPLETED);

        verify(meetingRepository, times(1)).findById(1L);
        verify(meetingRepository, times(1)).save(any(Meeting.class));
    }

    @Test
    void updateMeeting_throwsWhenNotFound() {
        MeetingRequest request = MeetingRequest.builder()
                .title("New Updated Meeting")
                .meetingDate(LocalDate.of(2026, 8, 1))
                .status(Meeting.Status.COMPLETED)
                .build();

        when(meetingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.updateMeeting(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteMeeting_deletesWhenFound() {
        Meeting existing = new Meeting();
        existing.setId(1L);
        existing.setTitle("Old Title");
        existing.setStatus(Meeting.Status.SCHEDULED);

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(meetingRepository).delete(any(Meeting.class));

        meetingService.deleteMeeting(1L);

        verify(meetingRepository, times(1)).findById(1L);
        verify(meetingRepository, times(1)).delete(existing);
    }

    @Test
    void deleteMeeting_throwsWhenNotFound() {
        when(meetingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.deleteMeeting(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
