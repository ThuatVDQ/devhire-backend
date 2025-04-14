package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.InterviewScheduleDTO;
import com.hcmute.devhire.DTOs.InterviewScheduleUpdateDTO;
import com.hcmute.devhire.entities.InterviewSchedule;

public interface IInterviewScheduleService {
    InterviewSchedule createInterviewSchedule(InterviewScheduleDTO dto) throws Exception;
    InterviewSchedule updateInterviewSchedule(Long id, InterviewScheduleUpdateDTO dto) throws Exception;

}
