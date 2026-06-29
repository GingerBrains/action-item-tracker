package com.laurel.actiontracker.service;

import com.laurel.actiontracker.dto.request.ActionItemRequest;
import com.laurel.actiontracker.dto.response.ActionItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ActionItemService {
    Page<ActionItemResponse> getAllActionItems(Pageable pageable);
    ActionItemResponse getActionItemById(Long id);
    ActionItemResponse createActionItem(ActionItemRequest request);
    ActionItemResponse updateActionItem(Long id, ActionItemRequest request);
    void deleteActionItem(Long id);

}
