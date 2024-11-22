package com.hcmute.devhire.responses;

import com.hcmute.devhire.DTOs.JobDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class UserListResponse {
    private List<UserResponse> users;
    int totalPages;
    int currentPage;
    int pageSize;
    Long totalElements;
}
