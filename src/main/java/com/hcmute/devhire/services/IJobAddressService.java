package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobAddressDTO;
import com.hcmute.devhire.entities.Address;

import java.util.List;

public interface IJobAddressService {
    List<Address> createJobAddresses(List<JobAddressDTO> jobAddressDTOS);
}
