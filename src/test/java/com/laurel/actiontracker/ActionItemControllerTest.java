package com.laurel.actiontracker;

import com.laurel.actiontracker.controller.ActionItemController;
import com.laurel.actiontracker.dto.request.ActionItemRequest;
import com.laurel.actiontracker.dto.response.ActionItemResponse;
import com.laurel.actiontracker.entity.ActionItem;
import com.laurel.actiontracker.entity.Meeting;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.security.CustomUserDetailsService;
import com.laurel.actiontracker.security.JwtUtil;
import com.laurel.actiontracker.service.ActionItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActionItemController.class)
@Import(ActionItemControllerTest.MethodSecurityTestConfig .class)
public class ActionItemControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActionItemService actionItemService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "MEMBER")
    void getAllActionItems_returns200() throws Exception {
        when(actionItemService.getAllActionItems(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/action-items"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getActionItemById_returns200() throws Exception {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setTitle("Test Meeting");
        meeting.setMeetingDate(LocalDate.now());
        meeting.setStatus(Meeting.Status.SCHEDULED);

        ActionItem actionItem = new ActionItem();
        actionItem.setId(1L);
        actionItem.setTitle("Test Action Item");
        actionItem.setDueDate(LocalDate.now().plusDays(7));
        actionItem.setStatus(ActionItem.Status.OPEN);
        actionItem.setMeeting(meeting);

        ActionItemResponse response = ActionItemResponse.from(actionItem);
        when(actionItemService.getActionItemById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/action-items/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Action Item"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getActionItemByID_returns404WhenNotFound() throws Exception {
        when(actionItemService.getActionItemById(99L))
                .thenThrow(new ResourceNotFoundException("Action Item not found with id : 99"));

        mockMvc.perform(get("/api/v1/action-items/{id}", 99L))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void createActionItem_returns201ForAdmin() throws Exception {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setTitle("Test Meeting");
        meeting.setMeetingDate(LocalDate.now());
        meeting.setStatus(Meeting.Status.SCHEDULED);

        ActionItem actionItem = new ActionItem();
        actionItem.setId(1L);
        actionItem.setTitle("Created Action Item");
        actionItem.setDueDate(LocalDate.now().plusDays(7));
        actionItem.setStatus(ActionItem.Status.IN_PROGRESS);
        actionItem.setPriority(ActionItem.Priority.HIGH);
        actionItem.setMeeting(meeting);

        ActionItemRequest request = ActionItemRequest.builder()
                .title("Created Action Item")
                .dueDate(LocalDate.now().plusDays(7))
                .status(ActionItem.Status.IN_PROGRESS)
                .priority(ActionItem.Priority.HIGH)
                .meetingId(1L)
                .build();

        ActionItemResponse response = ActionItemResponse.from(actionItem);
        when(actionItemService.createActionItem(any(ActionItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/action-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Created Action Item"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void createActionItem_returns403ForNonAdmin() throws Exception{
        ActionItemRequest request = ActionItemRequest.builder()
                .title("Created Action Item")
                .dueDate(LocalDate.now().plusDays(7))
                .status(ActionItem.Status.IN_PROGRESS)
                .priority(ActionItem.Priority.HIGH)
                .meetingId(1L)
                .build();

        mockMvc.perform(post("/api/v1/action-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateActionItem_returns200ForAdmin() throws Exception {
        Meeting meeting = new Meeting();
        meeting.setId(9L);
        meeting.setTitle("Test Meeting");
        meeting.setMeetingDate(LocalDate.now());
        meeting.setStatus(Meeting.Status.SCHEDULED);

        ActionItem updatedActionItem = new ActionItem();
        updatedActionItem.setId(1L);
        updatedActionItem.setTitle("Updated Action Item");
        updatedActionItem.setDueDate(LocalDate.now().plusDays(7));
        updatedActionItem.setStatus(ActionItem.Status.COMPLETED);
        updatedActionItem.setPriority(ActionItem.Priority.HIGH);
        updatedActionItem.setMeeting(meeting);

        ActionItemRequest request = ActionItemRequest.builder()
                .title("Original Action Item")
                .dueDate(LocalDate.now().plusDays(7))
                .status(ActionItem.Status.IN_PROGRESS)
                .priority(ActionItem.Priority.HIGH)
                .meetingId(9L)
                .build();

        ActionItemResponse response = ActionItemResponse.from(updatedActionItem);
        when(actionItemService.updateActionItem(eq(1L),any(ActionItemRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/action-items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Action Item"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void updateActionItem_returns403ForNonAdmin() throws Exception{
        ActionItemRequest request = ActionItemRequest.builder()
                .title("Original Action Item")
                .dueDate(LocalDate.now().plusDays(7))
                .status(ActionItem.Status.IN_PROGRESS)
                .priority(ActionItem.Priority.HIGH)
                .meetingId(9L)
                .build();

        mockMvc.perform(put("/api/v1/action-items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateActionItem_returns404WhenNotFound() throws Exception {
        ActionItemRequest request = ActionItemRequest.builder()
                .title("Original Action Item")
                .dueDate(LocalDate.now().plusDays(7))
                .status(ActionItem.Status.IN_PROGRESS)
                .priority(ActionItem.Priority.HIGH)
                .meetingId(9L)
                .build();

        when(actionItemService.updateActionItem(eq(99L), any(ActionItemRequest.class)))
                .thenThrow(new ResourceNotFoundException("Action Item not found with id: 99"));

        mockMvc.perform(put("/api/v1/action-items/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteActionItem_returns200ForAdmin() throws Exception{
        doNothing().when(actionItemService).deleteActionItem(1L);

        mockMvc.perform(delete("/api/v1/action-items/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void deleteActionItem_returns403ForNonAdmin() throws Exception{
        mockMvc.perform(delete("/api/v1/action-items/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteActionItem_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Action Item not found with id: 99"))
                .when(actionItemService).deleteActionItem(99L);

        mockMvc.perform(delete("/api/v1/action-items/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
