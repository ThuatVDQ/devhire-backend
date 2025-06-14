package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.entities.InterviewSchedule;
import com.hcmute.devhire.utils.InterviewResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IInterviewScheduleService {
    InterviewSchedule createInterviewSchedule(InterviewScheduleDTO dto) throws Exception;
    InterviewSchedule updateInterviewSchedule(Long id, InterviewScheduleUpdateDTO dto) throws Exception;
    Page<InterviewScheduleDTO> getAllInterviewSchedules(String username, PageRequest pageRequest) throws Exception;
    Page<InterviewScheduleDTO> getUserInterviewSchedules(String username, PageRequest pageRequest) throws Exception;
    Page<InterviewScheduleDTO> getByStatus(String username, String status, PageRequest pageRequest) throws Exception;
    List<InterviewSchedule> createBulkSchedules(InterviewScheduleBulkDTO dto) throws Exception;
    Page<InterviewScheduleDTO> getInterviewResults(String username, InterviewResult status, PageRequest pageRequest);
    InterviewSchedule updateInterviewResult(Long id, InterviewResultDTO dto) throws Exception;
    void sendEmail(Long id, EmailRequestDTO emailRequestDTO) throws Exception;
}
