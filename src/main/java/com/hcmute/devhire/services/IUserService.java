package com.hcmute.devhire.services;


import com.hcmute.devhire.DTOs.ProfileDTO;
import com.hcmute.devhire.DTOs.UserDTO;
import com.hcmute.devhire.entities.User;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String username, String password, Long roleId) throws Exception;
    User findById(Long id) throws Exception;
    UserDTO findByUsername(String username) throws Exception;
    UserDTO getProfile(String phone) throws Exception;
    User updateAvatar(String username, String avatarUrl) throws EntityNotFoundException;
    User updateProfile(String username, ProfileDTO profileDTO) throws EntityNotFoundException;
    User findByUserName(String username) throws Exception;
}
