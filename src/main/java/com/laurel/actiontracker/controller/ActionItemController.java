package com.laurel.actiontracker.controller;

import com.laurel.actiontracker.dto.request.ActionItemRequest;
import com.laurel.actiontracker.dto.response.ActionItemResponse;
import com.laurel.actiontracker.service.ActionItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/action-items")
public class ActionItemController {

    private final ActionItemService actionItemService;

    public ActionItemController(ActionItemService actionItemService) {
        this.actionItemService = actionItemService;
    }

    @GetMapping
    public ResponseEntity<Page<ActionItemResponse>> getAllActionItems(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(actionItemService.getAllActionItems(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActionItemResponse> getActionItemById(@PathVariable Long id){
        return ResponseEntity.ok((actionItemService.getActionItemById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActionItemResponse> createActionItem(@Valid @RequestBody ActionItemRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(actionItemService.createActionItem(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActionItemResponse> updateActionItem(@PathVariable Long id, @Valid @RequestBody ActionItemRequest request){
        return ResponseEntity.ok(actionItemService.updateActionItem(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteActionItem(@PathVariable Long id){
        actionItemService.deleteActionItem(id);
        return ResponseEntity.noContent().build();
    }

}
