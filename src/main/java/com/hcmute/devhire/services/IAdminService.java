package com.hcmute.devhire.services;

import com.hcmute.devhire.responses.DashboardResponse;


public interface IAdminService {
    DashboardResponse getDashboardData() throws Exception;
}
