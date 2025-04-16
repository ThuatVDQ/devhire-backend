package com.hcmute.devhire.controllers;
import com.hcmute.devhire.DTOs.InterviewScheduleDTO;
import com.hcmute.devhire.DTOs.InterviewScheduleUpdateDTO;
import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.entities.InterviewSchedule;
import com.hcmute.devhire.responses.PagedResponse;
import com.hcmute.devhire.services.IInterviewScheduleService;
import com.hcmute.devhire.utils.JobApplicationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @GetMapping()
    public ResponseEntity<?> getAllInterviewSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) JobApplicationStatus status
    ) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("interviewTime").descending());
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            Page<InterviewScheduleDTO> schedules = (status != null)
                    ? interviewScheduleService.getByStatus(username, String.valueOf(status), pageRequest)
                    : interviewScheduleService.getAllInterviewSchedules(username, pageRequest);

            return ResponseEntity.ok(new PagedResponse<>(
                    schedules.getContent(),
                    schedules.getNumber(),
                    schedules.getSize(),
                    schedules.getTotalElements(),
                    schedules.getTotalPages()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

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
