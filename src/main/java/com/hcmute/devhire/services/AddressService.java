package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.AddressDTO;
import com.hcmute.devhire.entities.Address;
import com.hcmute.devhire.repositories.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {
    private final AddressRepository addressRepository;

    @Override
    public Address createAddress(AddressDTO addressDTO) {
        Address address = Address.builder()
                .country(addressDTO.getCountry())
                .city(addressDTO.getCity())
                .district(addressDTO.getDistrict())
                .street(addressDTO.getStreet())
                .build();

        return addressRepository.save(address);
    }

    @Override
    public Address findById(Long addressId) {
        return addressRepository.findById(addressId).orElse(null);
    }
}
