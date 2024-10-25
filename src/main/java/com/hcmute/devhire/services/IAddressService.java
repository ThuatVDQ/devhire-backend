package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.AddressDTO;
import com.hcmute.devhire.entities.Address;

import java.util.Optional;

public interface IAddressService {
    Address createAddress(AddressDTO addressDTO);
    Address findById(Long addressId);
}
