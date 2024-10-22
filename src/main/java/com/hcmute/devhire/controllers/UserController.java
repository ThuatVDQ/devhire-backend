package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.UserDTO;
import com.hcmute.devhire.DTOs.UserLoginDTO;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.LoginResponse;
import com.hcmute.devhire.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String phone = null;

            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                phone = userDetails.getUsername();
            }

            if (phone == null) {
                return ResponseEntity.status(401).body("Unauthorized: No user found.");
            }

            UserDTO userDTO = userService.getProfile(phone);
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
                    userLoginDTO.getRoleId() == null ? 1 : userLoginDTO.getRoleId());
            UserDTO userDTO = userService.findByUsername(userLoginDTO.getUsername());

            return ResponseEntity.ok(LoginResponse
                    .builder()
                    .message("Login successfully")
                    .token(token)
                    .avatarUrl(safeGet(userDTO.getAvatarUrl()))
                    .roleId(userDTO.getRoleId())
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse
                    .builder()
                    .message("Login failed: " + e.getMessage())
                    .token(null)
                    .avatarUrl(null)
                    .roleId(null)
                    .build()
            );
        }
    }
    private String safeGet(String value) {
        return value != null ? value : "";
    }
    @PostMapping("/uploadAvatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file
    ) {

        return ResponseEntity.ok("Upload avatar successfully");
    }
}
