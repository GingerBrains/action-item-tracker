package com.laurel.actiontracker.controller;

import com.laurel.actiontracker.entity.ActionItem;
import com.laurel.actiontracker.service.ActionItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/action-items")
public class ActionItemController {

    private final ActionItemService actionItemService;

    public ActionItemController(ActionItemService actionItemService) {
        this.actionItemService = actionItemService;
    }

    @GetMapping
    public ResponseEntity<List<ActionItem>> getAllActionItems() {
        return ResponseEntity.ok(actionItemService.getAllActionItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActionItem> getActionItemById(@PathVariable Long id){
        return ResponseEntity.ok((actionItemService.getActionItemById(id)));
    }

    @PostMapping
    public ResponseEntity<ActionItem> createActionItem(@RequestBody ActionItem actionItem){
        return ResponseEntity.status(HttpStatus.CREATED).body(actionItemService.createActionItem(actionItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActionItem> updateActionItem(@PathVariable Long id, @RequestBody ActionItem actionItem){
        return ResponseEntity.ok(actionItemService.updateActionItem(id, actionItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActionItem(@PathVariable Long id){
        actionItemService.deleteActionItem(id);
        return ResponseEntity.noContent().build();
    }

}
