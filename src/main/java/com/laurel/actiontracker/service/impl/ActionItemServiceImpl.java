package com.laurel.actiontracker.service.impl;

import com.laurel.actiontracker.entity.ActionItem;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.repository.ActionItemRepository;
import com.laurel.actiontracker.service.ActionItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActionItemServiceImpl implements ActionItemService {

    private final ActionItemRepository actionItemRepository;

    public ActionItemServiceImpl(ActionItemRepository actionItemRepository) {
        this.actionItemRepository = actionItemRepository;
    }


    @Override
    public List<ActionItem> getAllActionItems() {
        return actionItemRepository.findAll();
    }

    @Override
    public ActionItem getActionItemById(Long id) {
        return actionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action Item not found with id " + id)) ;
    }

    @Override
    public ActionItem createActionItem(ActionItem actionItem) {
        return actionItemRepository.save(actionItem);
    }

    @Override
    public ActionItem updateActionItem(Long id, ActionItem actionItem) {
        ActionItem existing = getActionItemById(id);
        existing.setTitle(actionItem.getTitle());
        existing.setDescription(actionItem.getDescription());
        existing.setStatus(actionItem.getStatus());
        existing.setDueDate(actionItem.getDueDate());
        existing.setMeeting(actionItem.getMeeting());
        existing.setAssignee(actionItem.getAssignee());
        return actionItemRepository.save(existing);
    }

    @Override
    public void deleteActionItem(Long id) {
        ActionItem existing = getActionItemById(id);
        actionItemRepository.delete(existing);
    }
}
