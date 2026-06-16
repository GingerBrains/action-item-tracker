package com.laurel.actiontracker.repository;

import com.laurel.actiontracker.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}
