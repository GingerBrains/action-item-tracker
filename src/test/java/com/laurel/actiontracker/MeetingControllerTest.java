package com.laurel.actiontracker;

import com.laurel.actiontracker.controller.MeetingController;
import com.laurel.actiontracker.dto.request.MeetingRequest;
import com.laurel.actiontracker.dto.response.MeetingResponse;
import com.laurel.actiontracker.entity.Meeting;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.service.MeetingService;
import com.laurel.actiontracker.security.JwtUtil;
import com.laurel.actiontracker.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingController.class)
@Import(MeetingControllerTest.MethodSecurityTestConfig.class)
public class MeetingControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeetingService meetingService;


    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "MEMBER")
    void getAllMeetings_returns200() throws Exception {
        when(meetingService.getAllMeetings()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/meetings"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMeetingById_returns200() throws Exception {
        MeetingResponse response = MeetingResponse.from(new Meeting());
        when(meetingService.getMeetingById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/meetings/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMeetingByID_returns404WhenNotFound() throws Exception {
        when(meetingService.getMeetingById(99L))
                .thenThrow(new ResourceNotFoundException("Meeting not found with id : 99"));

        mockMvc.perform(get("/api/v1/meetings/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMeeting_returns201ForAdmin() throws Exception{
        MeetingRequest request = MeetingRequest.builder()
                .title("Test Meeting")
                .meetingDate(LocalDate.of(2026, 8, 1))
                .status(Meeting.Status.SCHEDULED)
                .build();

        MeetingResponse response = MeetingResponse.from(new Meeting());
        when(meetingService.createMeeting(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void createMeeting_returns403ForNonAdmin() throws Exception{
        MeetingRequest request = MeetingRequest.builder()
                .title("Test Meeting")
                .meetingDate(LocalDate.of(2026, 8, 1))
                .status(Meeting.Status.SCHEDULED)
                .build();

        mockMvc.perform(post("/api/v1/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMeeting_returns200ForAdmin() throws Exception{
        MeetingRequest request = MeetingRequest.builder()
                .title("Test Meeting")
                .meetingDate(LocalDate.of(2026, 8, 1))
                .status(Meeting.Status.SCHEDULED)
                .build();

        MeetingResponse response = MeetingResponse.from(new Meeting());
        when(meetingService.updateMeeting(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/meetings/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void updateMeeting_returns403ForNonAdmin() throws Exception{
        MeetingRequest request = MeetingRequest.builder()
                .title("Test Meeting")
                .meetingDate(LocalDate.of(2026, 8, 1))
                .status(Meeting.Status.SCHEDULED)
                .build();

        mockMvc.perform(put("/api/v1/meetings/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMeeting_returns404WhenNotFound() throws Exception {
        MeetingRequest request = MeetingRequest.builder()
                .title("Test Meeting")
                .meetingDate(LocalDate.of(2026, 8, 1))
                .status(Meeting.Status.SCHEDULED)
                .build();

        when(meetingService.updateMeeting(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Meeting not found with id: 99"));

        mockMvc.perform(put("/api/v1/meetings/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMeeting_returns200ForAdmin() throws Exception{
        doNothing().when(meetingService).deleteMeeting(1L);

        mockMvc.perform(delete("/api/v1/meetings/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void deleteMeeting_returns403ForNonAdmin() throws Exception{
        mockMvc.perform(delete("/api/v1/meetings/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMeeting_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Meeting not found with id: 99"))
                .when(meetingService).deleteMeeting(99L);

        mockMvc.perform(delete("/api/v1/meetings/{id}", 99L))
                .andExpect(status().isNotFound());
    }

}
