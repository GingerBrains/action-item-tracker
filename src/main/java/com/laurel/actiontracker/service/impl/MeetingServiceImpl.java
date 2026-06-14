package com.laurel.actiontracker.service.impl;

import com.laurel.actiontracker.entity.Meeting;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.repository.MeetingRepository;
import com.laurel.actiontracker.service.MeetingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MeetingServiceImpl implements MeetingService {
    private final MeetingRepository meetingRepository;

    public MeetingServiceImpl(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    @Override
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    @Override
    public Meeting getMeetingById(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));
    }

    @Override
    public Meeting createMeeting(Meeting meeting) {
        return meetingRepository.save(meeting);
    }

    @Override
    public Meeting updateMeeting(Long id, Meeting meeting) {
        Meeting existing = getMeetingById(id);
        existing.setTitle(meeting.getTitle());
        existing.setDescription(meeting.getDescription());
        existing.setMeetingDate(meeting.getMeetingDate());
        existing.setStatus(meeting.getStatus());
        return meetingRepository.save(existing);
    }

    @Override
    public void deleteMeeting(Long id) {
        Meeting existing = getMeetingById(id);
        meetingRepository.delete(existing);
    }
}
