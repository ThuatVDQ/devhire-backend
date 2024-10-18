package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.AddressDTO;
import com.hcmute.devhire.entities.Address;

public interface IAddressService {
    Address createAddress(AddressDTO addressDTO);
}
