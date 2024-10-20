package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.UserDTO;
import com.hcmute.devhire.DTOs.UserLoginDTO;
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
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            // Lấy thông tin xác thực hiện tại từ SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String phone = null;

            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                phone = userDetails.getUsername(); // Số điện thoại được sử dụng làm username
            }

            // Kiểm tra nếu không tìm thấy thông tin người dùng
            if (phone == null) {
                return ResponseEntity.status(401).body("Unauthorized: No user found.");
            }

            // Lấy thông tin người dùng từ cơ sở dữ liệu dựa trên số điện thoại
            UserDTO userDTO = userService.getProfile(phone);
            if (userDTO == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            // Bạn có thể trả về thông tin người dùng trực tiếp hoặc thông qua DTO
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
                    userLoginDTO.getPhone(),
                    userLoginDTO.getPassword(),
                    userLoginDTO.getRoleId() == null ? 1 : userLoginDTO.getRoleId());

            return ResponseEntity.ok(LoginResponse
                    .builder()
                    .message("Login successfully")
                    .token(token)
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse
                    .builder()
                    .message("Login failed: " + e.getMessage())
                    .token(null)
                    .build()
            );
        }
    }
}
