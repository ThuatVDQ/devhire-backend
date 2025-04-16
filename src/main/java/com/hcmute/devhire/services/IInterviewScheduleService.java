package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.InterviewScheduleDTO;
import com.hcmute.devhire.DTOs.InterviewScheduleUpdateDTO;
import com.hcmute.devhire.entities.InterviewSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IInterviewScheduleService {
    InterviewSchedule createInterviewSchedule(InterviewScheduleDTO dto) throws Exception;
    InterviewSchedule updateInterviewSchedule(Long id, InterviewScheduleUpdateDTO dto) throws Exception;
    Page<InterviewScheduleDTO> getAllInterviewSchedules(String username, PageRequest pageRequest) throws Exception;
    Page<InterviewScheduleDTO> getByStatus(String username, String status, PageRequest pageRequest) throws Exception;
}
