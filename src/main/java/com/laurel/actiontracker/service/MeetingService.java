package com.laurel.actiontracker.service;

import com.laurel.actiontracker.dto.request.MeetingRequest;
import com.laurel.actiontracker.dto.response.MeetingResponse;

import java.util.List;

public interface MeetingService {

    List<MeetingResponse> getAllMeetings();
    MeetingResponse getMeetingById(Long id);
    MeetingResponse createMeeting(MeetingRequest request);
    MeetingResponse updateMeeting(Long id, MeetingRequest request);
    void deleteMeeting(Long id);
}
