package com.hcmute.devhire.services;


import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.UserResponse;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String username, String password, Long roleId) throws Exception;
    User findById(Long id) throws Exception;
    UserDTO findByUsername(String username) throws Exception;
    UserDTO getProfile(String email) throws Exception;
    User updateAvatar(String username, String avatarUrl) throws EntityNotFoundException;
    User updateProfile(String username, ProfileDTO profileDTO) throws EntityNotFoundException;
    User findByUserName(String username) throws Exception;
    void verifyUser(VerifyUserDTO input) throws Exception;
    void resendVerificationCode(String email) throws Exception;
    void forgotPassword(String email) throws Exception;
    void updatePassword(UpdatePasswordDTO updatePasswordDTO, String email) throws Exception;
    void resetPassword(ResetPasswordDTO resetPasswordDTO) throws Exception;
    int countUsers() throws Exception;
    int countUsersMonthly(int month, int year) throws Exception;
    List<UserResponse> getAllUsers() throws Exception;
}
