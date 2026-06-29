package com.laurel.actiontracker.service.impl;

import com.laurel.actiontracker.dto.request.ActionItemRequest;
import com.laurel.actiontracker.dto.response.ActionItemResponse;
import com.laurel.actiontracker.entity.ActionItem;
import com.laurel.actiontracker.entity.Meeting;
import com.laurel.actiontracker.entity.User;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.repository.ActionItemRepository;
import com.laurel.actiontracker.repository.MeetingRepository;
import com.laurel.actiontracker.repository.UserRepository;
import com.laurel.actiontracker.service.ActionItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActionItemServiceImpl implements ActionItemService {

    private final ActionItemRepository actionItemRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    public ActionItemServiceImpl(ActionItemRepository actionItemRepository, MeetingRepository meetingRepository, UserRepository userRepository) {
        this.actionItemRepository = actionItemRepository;
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ActionItemResponse> getAllActionItems(Pageable pageable) {
        return actionItemRepository.findAll(pageable).map(ActionItemResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ActionItemResponse getActionItemById(Long id) {
        ActionItem actionItem = findActionItemOrThrow(id);
        return ActionItemResponse.from(actionItem);
    }

    @Override
    public ActionItemResponse createActionItem(ActionItemRequest request) {
        ActionItem actionItem = request.toEntity();

        Meeting meeting = meetingRepository.findById(request.getMeetingId())
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id : " + request.getMeetingId()));
        actionItem.setMeeting(meeting);

        if(request.getAssigneeId() != null){
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id : " + request.getAssigneeId()));
            actionItem.setAssignee(assignee);
        }
        return ActionItemResponse.from(actionItemRepository.save(actionItem));

    }


    @Override
    public ActionItemResponse updateActionItem(Long id, ActionItemRequest request) {
        ActionItem existing = findActionItemOrThrow(id);
        ActionItem updated = request.toEntity();
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setStatus(updated.getStatus());
        existing.setPriority(updated.getPriority());
        existing.setDueDate(updated.getDueDate());

        Meeting meeting = meetingRepository.findById(request.getMeetingId())
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id : " + request.getMeetingId()));
        existing.setMeeting(meeting);

        if(request.getAssigneeId() != null){
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id : " + request.getAssigneeId()));
            existing.setAssignee(assignee);
        }

        return ActionItemResponse.from(actionItemRepository.save(existing));
    }

    @Override
    public void deleteActionItem(Long id) {
        ActionItem existing = findActionItemOrThrow(id);
        actionItemRepository.delete(existing);
    }

    private ActionItem findActionItemOrThrow(Long id) {
        return actionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action Item not found with id: " + id));
    }
}
