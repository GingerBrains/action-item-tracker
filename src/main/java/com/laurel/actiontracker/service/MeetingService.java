package com.laurel.actiontracker.service;

import com.laurel.actiontracker.entity.Meeting;

import java.util.List;

public interface MeetingService {

    List<Meeting> getAllMeetings();
    Meeting getMeetingById(Long id);
    Meeting createMeeting(Meeting meeting);
    Meeting updateMeeting(Long id, Meeting meeting);
    void deleteMeeting(Long id);
}
