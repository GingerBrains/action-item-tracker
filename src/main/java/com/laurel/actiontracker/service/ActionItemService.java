package com.laurel.actiontracker.service;

import com.laurel.actiontracker.dto.request.ActionItemRequest;
import com.laurel.actiontracker.dto.response.ActionItemResponse;

import java.util.List;


public interface ActionItemService {
    List<ActionItemResponse> getAllActionItems();
    ActionItemResponse getActionItemById(Long id);
    ActionItemResponse createActionItem(ActionItemRequest request);
    ActionItemResponse updateActionItem(Long id, ActionItemRequest request);
    void deleteActionItem(Long id);

}
