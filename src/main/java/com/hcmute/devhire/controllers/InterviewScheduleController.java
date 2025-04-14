package com.hcmute.devhire.controllers;
import com.hcmute.devhire.DTOs.InterviewScheduleDTO;
import com.hcmute.devhire.DTOs.InterviewScheduleUpdateDTO;
import com.hcmute.devhire.entities.InterviewSchedule;
import com.hcmute.devhire.services.IInterviewScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interview-schedules")
public class InterviewScheduleController {
    private final IInterviewScheduleService interviewScheduleService;

    @PostMapping("/create")
    public ResponseEntity<?> createInterviewSchedule(
            @Valid @RequestBody InterviewScheduleDTO dto,
            BindingResult result) throws Exception {
        try {
            InterviewSchedule interviewScheduleDTO = interviewScheduleService.createInterviewSchedule(dto);
            if (result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            return ResponseEntity.ok(interviewScheduleDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Cập nhật lịch phỏng vấn
    @PostMapping("/{id}")
    public ResponseEntity<?> updateInterviewSchedule(
            @PathVariable Long id,
            @Valid @RequestBody InterviewScheduleUpdateDTO dto,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }
        return ResponseEntity.ok(interviewScheduleService.updateInterviewSchedule(id, dto));
    }
}
