package com.hcmute.devhire.services;


import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.entities.Role;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.RoleRepository;
import com.hcmute.devhire.repositories.UserRepository;
import com.hcmute.devhire.responses.UserResponse;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.hcmute.devhire.components.JwtUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;


import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final IEmailService emailService;
    private final JwtUtil jwtUtil;
    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws Exception {
        String phone = userDTO.getPhone();
        String email = userDTO.getEmail();

        if (phone != null && !phone.isEmpty() && userRepository.existByPhoneNumber(phone)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }

        if (email != null && !email.isEmpty() && userRepository.existByEmail(email)) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        Role role = roleRepository.findById(
                userDTO.getRoleId()).orElseThrow(() -> new Exception("Role not found"));

        if (role.getName().toUpperCase().equals(Role.ADMIN)) {
            throw new Exception("Cannot create admin user");
        }
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phone(userDTO.getPhone() != null ? userDTO.getPhone() : "")
                .email(userDTO.getEmail() != null ? userDTO.getEmail() : "")
                .build();

        newUser.setRole(role);

        String password = userDTO.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword);

        newUser.setVerificationCode(generateVerificationCode());
        newUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        newUser.setEnabled(false);
        sendVerificationEmail(newUser);

        return userRepository.save(newUser);
    }

    @Override
    public void verifyUser(VerifyUserDTO input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid verification code");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    @Override
    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    @Override
    public void forgotPassword(String email) throws Exception {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            sendVerificationEmail(user);
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO, String email) throws Exception {

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (passwordEncoder.matches(updatePasswordDTO.getPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));

                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid password");
            }
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) throws Exception {
        Optional<User> optionalUser = userRepository.findByEmail(resetPasswordDTO.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public int countUsers() throws Exception {
        return userRepository.countUsers();
    }

    @Override
    public int countUsersMonthly(int month, int year) throws Exception {
        return userRepository.countUsersMonthly(month, year);
    }

    @Override
    public List<UserResponse> getAllUsers() throws Exception {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new Exception("No user found");
        }
        return users.stream().map(UserResponse::convertFromUser).toList();
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email");
        }
    }
    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
    @Override
    public String login(String username, String password, Long roleId) throws Exception {
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isPresent()) {
            User existingUser = user.get();

            if (!passwordEncoder.matches(password, existingUser.getPassword())) {
                throw new Exception("Invalid phone number or password");
            }
            if (!Objects.equals(existingUser.getRole().getId(), roleId)) {
                throw new Exception("Invalid role");
            }

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            //authenticate with java spring security
            authenticationManager.authenticate(authenticationToken);

            if (!existingUser.isEnabled()) {
                throw new Exception("Account is not verified");
            }

            return jwtUtil.generateToken(existingUser);
        } else {
            throw new Exception("User not found");
        }

    }

    @Override
    public User findById(Long id) throws Exception {
        return userRepository.findById(id).orElseThrow(() -> new Exception("User not found"));
    }

    public User findByUserName(String username) throws Exception {
        Optional<User> userOptional;

        if (username.contains("@")) {
            userOptional = userRepository.findByEmail(username);
        } else {
            userOptional = userRepository.findByPhone(username);
        }

        if (userOptional.isEmpty()) {
            throw new Exception("User not found with username: " + username);
        }

        return userOptional.get();
    }

    @Override
    public UserDTO findByUsername(String username) throws Exception {
        Optional<User> userOptional;

        if (username.contains("@")) {
            userOptional = userRepository.findByEmail(username);
        } else {
            userOptional = userRepository.findByPhone(username);
        }

        if (userOptional.isEmpty()) {
            throw new Exception("User not found with username: " + username);
        }

        User user = userOptional.get();

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .roleId(user.getRole().getId())
                .build();
    }

    @Override
    public UserDTO getProfile(String email) throws Exception {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new Exception("User not found"));
        return UserDTO.builder()
                .fullName(safeGet(user.getFullName()))
                .phone(safeGet(user.getPhone()))
                .email(safeGet(user.getEmail()))
                .avatarUrl(safeGet(user.getAvatarUrl()))
                .introduction(safeGet(user.getIntroduction()))
                .gender(safeGet(user.getGender()))
                .status(user.getStatus() != null ? user.getStatus().name() : "UNKNOWN")
                .build();
    }

    @Override
    public User updateAvatar(String username, String avatarUrl) throws EntityNotFoundException {
        User user = username.contains("@")
                ? userRepository.findByEmail(username).orElseThrow(() -> new EntityNotFoundException("User not found"))
                : userRepository.findByPhone(username).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (avatarUrl == null) {
            user.setAvatarUrl("");
            userRepository.save(user);
            return user;
        }
        if (!avatarUrl.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
        }

        return user;
    }

    @Override
    public User updateProfile(String username, ProfileDTO profileDTO) throws EntityNotFoundException {
        User user = username.contains("@")
                ? userRepository.findByEmail(username).orElseThrow(() -> new EntityNotFoundException("User not found"))
                : userRepository.findByPhone(username).orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (profileDTO.getFullName() != null) {
            user.setFullName(profileDTO.getFullName());
        }

        if (profileDTO.getGender() != null) {
            user.setGender(profileDTO.getGender());
        }
        if (profileDTO.getIntroduction() != null) {
            user.setIntroduction(profileDTO.getIntroduction());
        }

        return userRepository.save(user);
    }

    private String safeGet(String value) {
        return value != null ? value : "";
    }

}
