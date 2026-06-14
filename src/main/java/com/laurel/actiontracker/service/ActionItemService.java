package com.laurel.actiontracker.service;

import com.laurel.actiontracker.entity.ActionItem;

import java.util.List;


public interface ActionItemService {
    List<ActionItem> getAllActionItems();
    ActionItem getActionItemById(Long id);
    ActionItem createActionItem(ActionItem actionItem);
    ActionItem updateActionItem(Long id, ActionItem actionItem);
    void deleteActionItem(Long id);

}
