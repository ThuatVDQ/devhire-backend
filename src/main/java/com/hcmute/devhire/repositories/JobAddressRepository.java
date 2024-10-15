package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobAddressRepository extends JpaRepository<Address, Long> {
}
