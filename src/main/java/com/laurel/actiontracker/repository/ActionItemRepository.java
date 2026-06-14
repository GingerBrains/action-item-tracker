package com.laurel.actiontracker.repository;

import com.laurel.actiontracker.entity.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {
}
