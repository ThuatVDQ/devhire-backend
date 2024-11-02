package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.ProfileDTO;
import com.hcmute.devhire.DTOs.UserDTO;
import com.hcmute.devhire.DTOs.UserLoginDTO;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.LoginResponse;
import com.hcmute.devhire.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
