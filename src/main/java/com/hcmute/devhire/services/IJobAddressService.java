package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobAddressDTO;
import com.hcmute.devhire.entities.JobAddress;

import java.util.List;

public interface IJobAddressService {
    List<JobAddress> createJobAddresses(List<JobAddressDTO> jobAddressDTOS);
}
