package com.laurel.actiontracker.controller;

import com.laurel.actiontracker.dto.request.ActionItemRequest;
import com.laurel.actiontracker.dto.response.ActionItemResponse;
import com.laurel.actiontracker.service.ActionItemService;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<ActionItemResponse>> getAllActionItems() {
        return ResponseEntity.ok(actionItemService.getAllActionItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActionItemResponse> getActionItemById(@PathVariable Long id){
        return ResponseEntity.ok((actionItemService.getActionItemById(id)));
    }

    @PostMapping
    public ResponseEntity<ActionItemResponse> createActionItem(@Valid @RequestBody ActionItemRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(actionItemService.createActionItem(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActionItemResponse> updateActionItem(@PathVariable Long id, @Valid @RequestBody ActionItemRequest request){
        return ResponseEntity.ok(actionItemService.updateActionItem(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActionItem(@PathVariable Long id){
        actionItemService.deleteActionItem(id);
        return ResponseEntity.noContent().build();
    }

}
