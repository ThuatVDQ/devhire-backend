package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.LoginResponse;
import com.hcmute.devhire.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final FileUtil fileUtil;
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {

            String username = getAuthenticatedUsername();

            UserDTO userDTO = userService.getProfile(username);
            if (userDTO == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            return ResponseEntity.ok(userDTO);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            if (userDTO.getPhone() == null && userDTO.getEmail() == null) {
                return ResponseEntity.badRequest().body("Phone or email is required");
            }
            if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                return ResponseEntity.badRequest().body("Password not match");
            }
            User user = userService.createUser(userDTO);
            //return ResponseEntity.ok("Register successfully");
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(
            @Valid @RequestBody VerifyUserDTO verifyUserDTO,
            BindingResult result
            ) {
        if(result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }
        try {
            userService.verifyUser(verifyUserDTO);
            return ResponseEntity.ok("Account verified successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        try {
            userService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification code sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO
    ) {
        try {
            String token = userService.login(
                    userLoginDTO.getUsername(),
                    userLoginDTO.getPassword(),
                    userLoginDTO.getRoleId() == null ? 3 : userLoginDTO.getRoleId());
            UserDTO userDTO = userService.findByUsername(userLoginDTO.getUsername());

            return ResponseEntity.ok(LoginResponse
                    .builder()
                    .message("Login successfully")
                    .token(token)
                    .roleId(userDTO.getRoleId())
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse
                    .builder()
                    .message("Login failed: " + e.getMessage())
                    .token(null)
                    .roleId(null)
                    .build()
            );
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            userService.forgotPassword(email);
            return ResponseEntity.ok("Verification code sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<?> verifyForgotPassword(
            @Valid @RequestBody VerifyUserDTO verifyUserDTO,
            BindingResult result
    ) {
        if(result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }
        try {
            userService.verifyUser(verifyUserDTO);
            return ResponseEntity.ok("Verification code verified");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordDTO resetPasswordDTO,
            BindingResult result
    ) {
        if(result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }
        try {
            if (!resetPasswordDTO.isPasswordMatching()) {
                return ResponseEntity.badRequest().body("Password not match");
            }
            userService.resetPassword(resetPasswordDTO);
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password/resend")
    public ResponseEntity<?> forgotPasswordResendVerificationCode(@RequestParam String email) {
        try {
            userService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification code sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordDTO updatePasswordDTO, BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            String email = getAuthenticatedUsername();
            if (email == null) {
                return ResponseEntity.badRequest().body("Unauthorized: No user found.");
            }
            if (!updatePasswordDTO.isPasswordMatching()) {
                return ResponseEntity.badRequest().body("Password not match");
            }
            userService.updatePassword(updatePasswordDTO, email);
            return ResponseEntity.ok("Update password successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {



        return ResponseEntity.ok("Logout successfully");
    }
    @PostMapping("/uploadAvatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String username = getAuthenticatedUsername();

        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized: No user found.");
        }

        if (!fileUtil.isImageFormatValid(file)) {
            return ResponseEntity.badRequest().body("Invalid image format");
        }
        String filename;
        try {
            filename = fileUtil.storeFile(file);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error occurred while saving file: " + e.getMessage());
        }

        try {
            User user = userService.updateAvatar(username, filename);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to update avatar: " + e.getMessage());
        }

        return ResponseEntity.ok("Upload avatar successfully");
    }
    @PutMapping("/deleteAvatar")
    public ResponseEntity<?> deleteAvatar() {
        String username = getAuthenticatedUsername();

        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized: No user found.");
        }

        try {
            User user = userService.updateAvatar(username, null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete avatar: " + e.getMessage());
        }

        return ResponseEntity.ok("Delete avatar successfully");
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileDTO profileDTO, BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            String username = getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.status(401).body("Unauthorized: No user found.");
            }
            User user = userService.updateProfile(username, profileDTO);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }
}
