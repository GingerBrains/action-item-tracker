package com.laurel.actiontracker.service.impl;

import com.laurel.actiontracker.dto.request.MeetingRequest;
import com.laurel.actiontracker.dto.response.MeetingResponse;
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
    public List<MeetingResponse> getAllMeetings() {
        return meetingRepository.findAll().stream()
                .map(MeetingResponse::from)
                .toList();
    }

    @Override
    public MeetingResponse getMeetingById(Long id) {
        Meeting meeting = findMeetingOrThrow(id);
        return MeetingResponse.from(meeting);
    }

    @Override
    public MeetingResponse createMeeting(MeetingRequest request) {
        return MeetingResponse.from(meetingRepository.save(request.toEntity()));
    }

    @Override
    public MeetingResponse updateMeeting(Long id, MeetingRequest request) {
        Meeting existing = findMeetingOrThrow(id);
        Meeting updated = request.toEntity();
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setMeetingDate(updated.getMeetingDate());
        existing.setStatus(updated.getStatus());
        return MeetingResponse.from(meetingRepository.save(existing));
    }

    @Override
    public void deleteMeeting(Long id) {
        Meeting existing = findMeetingOrThrow(id);
        meetingRepository.delete(existing);
    }

    private Meeting findMeetingOrThrow(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));
    }
}
