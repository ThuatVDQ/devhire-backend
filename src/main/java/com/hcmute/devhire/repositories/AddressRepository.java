package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Address findAddressByCityAndDistrictAndStreet(String city, String district, String street);
}
