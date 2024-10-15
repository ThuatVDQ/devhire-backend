package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.Address;
import com.hcmute.devhire.DTOs.JobAddressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobAddressService implements IJobAddressService {
    @Override
    public List<Address> createJobAddresses(List<JobAddressDTO> jobAddressDTOs) {
        return jobAddressDTOs.stream()
                .map(addressDTO -> Address.builder()
                        .country(addressDTO.getCountry())
                        .city(addressDTO.getCity())
                        .district(addressDTO.getDistrict())
                        .street(addressDTO.getStreet())
                        .build())
                .collect(Collectors.toList());
    }
}
