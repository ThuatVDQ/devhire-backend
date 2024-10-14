package com.hcmute.devhire.services;


import com.hcmute.devhire.DTOs.UserDTO;
import com.hcmute.devhire.entities.User;

import java.util.Optional;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String phone, String password, Long roleId) throws Exception;
    User findById(Long id) throws Exception;
}
