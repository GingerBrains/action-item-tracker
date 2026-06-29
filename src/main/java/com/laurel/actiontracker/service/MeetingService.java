package com.laurel.actiontracker.service;

import com.laurel.actiontracker.dto.request.MeetingRequest;
import com.laurel.actiontracker.dto.response.MeetingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetingService {

    Page<MeetingResponse> getAllMeetings(Pageable pageable);
    MeetingResponse getMeetingById(Long id);
    MeetingResponse createMeeting(MeetingRequest request);
    MeetingResponse updateMeeting(Long id, MeetingRequest request);
    void deleteMeeting(Long id);
}
