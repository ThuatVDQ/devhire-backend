package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.ApplicationListResponse;
import com.hcmute.devhire.responses.LoginResponse;
import com.hcmute.devhire.services.IJobApplicationService;
import com.hcmute.devhire.services.INotificationService;
import com.hcmute.devhire.services.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final FileUtil fileUtil;
    private final INotificationService notificationService;
    private final JwtUtil jwtUtil;
    private final IJobApplicationService jobApplicationService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {

            String username = JwtUtil.getAuthenticatedUsername();

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
            notificationService.createAndSendNotification("Welcome to DevHire!", user.getUsername());
            notificationService.sendNotificationToAdmin("New user registered: " + user.getUsername());
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

            if (Objects.equals(userDTO.getStatus(), "INACTIVE")) {
                return ResponseEntity.badRequest().body(LoginResponse
                        .builder()
                        .message("Your account has been banned")
                        .token(null)
                        .roleId(null)
                        .build()
                );

            }
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

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordDTO updatePasswordDTO, BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            String email = JwtUtil.getAuthenticatedUsername();
            if (email == null) {
                return ResponseEntity.badRequest().body("Unauthorized: No user found.");
            }
            if (!updatePasswordDTO.isPasswordMatching()) {
                return ResponseEntity.badRequest().body("Password not match");
            }
            userService.updatePassword(updatePasswordDTO, email);
            notificationService.createAndSendNotification("Password updated", email);
            notificationService.sendNotificationToAdmin("User: "+email + " updated new password");
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
        String username = JwtUtil.getAuthenticatedUsername();

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
            notificationService.sendNotificationToAdmin("User: "+ user.getFullName() + " just updated avatar");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to update avatar: " + e.getMessage());
        }

        return ResponseEntity.ok("Upload avatar successfully");
    }
    @PutMapping("/deleteAvatar")
    public ResponseEntity<?> deleteAvatar() {
        String username = JwtUtil.getAuthenticatedUsername();

        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized: No user found.");
        }

        try {
            User user = userService.updateAvatar(username, null);
            notificationService.sendNotificationToAdmin("User: "+ user.getFullName() + " just deleted avatar");
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
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.status(401).body("Unauthorized: No user found.");
            }
            User user = userService.updateProfile(username, profileDTO);
            notificationService.sendNotificationToAdmin("User: "+ user.getFullName() + " just updated profile");
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/auth/google")
    public ResponseEntity<?> loginGoogle() {
        try {
            String authUrl = userService.generateAuthUrl("google");
            if (authUrl == null) {
                return ResponseEntity.badRequest().body("Invalid login type");
            }

            return ResponseEntity.ok(authUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to generate auth url");
        }
    }

    @GetMapping("/auth/google/callback")
    public ResponseEntity<?> callback(
            @RequestParam("code") String code,
            @RequestParam("role_id") Long roleId,
            HttpServletRequest request)
    {
        try {
            UserDTO userInfo = userService.authenticateAndFetchProfile(code, "google");
            if (userInfo == null) {
                return ResponseEntity.badRequest().body("Failed to authenticate");
            }
            userInfo.setRoleId(roleId);

            return this.loginGoogle(userInfo, request);
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

    @GetMapping("/auth/facebook")
    public ResponseEntity<?> loginFacebook() {
        try {
            String authUrl = userService.generateAuthUrl("facebook");
            if (authUrl == null) {
                return ResponseEntity.badRequest().body("Invalid login type");
            }

            return ResponseEntity.ok(authUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to generate auth url");
        }
    }

    @GetMapping("/auth/facebook/callback")
    public ResponseEntity<?> callbackFacebook(
            @RequestParam("code") String code,
            @RequestParam("role_id") Long roleId,
            HttpServletRequest request)
    {
        try {
            UserDTO userInfo = userService.authenticateAndFetchProfile(code, "facebook");
            if (userInfo == null) {
                return ResponseEntity.badRequest().body("Failed to authenticate");
            }
            userInfo.setRoleId(roleId);

            return this.loginFacebook(userInfo, request);
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

    private ResponseEntity<?> loginFacebook(
            @RequestBody UserDTO userDTO,
            HttpServletRequest request
    ) throws Exception {
        String token = userService.loginFacebook(userDTO);

        String username = jwtUtil.extractUsername(token);
        User userDetail = userService.findByUserName(username);
        LoginResponse loginResponse = LoginResponse.builder()
                .message("Login successfully")
                .token(token)
                .roleId(userDetail.getRole().getId())
                .build();

        return ResponseEntity.ok().body(loginResponse);
    }

    @GetMapping("/getApplications")
    public ResponseEntity<?> getApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (page < 0 || limit <= 0) {
            return ResponseEntity.badRequest().body("Page must be >= 0 and size must be > 0.");
        }

        try {
            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("id").descending()
            );
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.badRequest().body("Unauthorized: No user found.");
            }
            User user = userService.findByUserName(username);

            Page<JobApplicationDTO> applications = jobApplicationService.getByUserId(user.getId(), pageRequest);

            if (applications.isEmpty()) {
                return ResponseEntity.badRequest().body("No applications found");
            }
            ApplicationListResponse response = ApplicationListResponse.builder()
                    .applications(applications.getContent())
                    .totalPages(applications.getTotalPages())
                    .currentPage(page)
                    .pageSize(limit)
                    .totalElements(applications.getTotalElements())
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private ResponseEntity<?> loginGoogle(
            @RequestBody UserDTO userDTO,
            HttpServletRequest request
    ) throws Exception {
        String token = userService.loginGoogle(userDTO);

        String username = jwtUtil.extractUsername(token);
        User userDetail = userService.findByUserName(username);
        LoginResponse loginResponse = LoginResponse.builder()
                .message("Login successfully")
                .token(token)
                .roleId(userDetail.getRole().getId())
                .build();

        return ResponseEntity.ok().body(loginResponse);
    }

    @PostMapping("/contact")
    public ResponseEntity<?> contact(@Valid @RequestBody EmailRequestDTO emailRequestDTO, BindingResult result) {
        if(result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }
        try {
            userService.contactAdmin(emailRequestDTO);
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
