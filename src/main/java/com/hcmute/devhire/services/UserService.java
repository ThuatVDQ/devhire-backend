package com.hcmute.devhire.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.entities.Role;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.RoleRepository;
import com.hcmute.devhire.repositories.UserRepository;
import com.hcmute.devhire.repositories.specification.UserSpecifications;
import com.hcmute.devhire.responses.UserResponse;
import com.hcmute.devhire.utils.Status;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;
import com.hcmute.devhire.components.JwtUtil;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String facebookRedirectUri;

    @Value("${spring.security.oauth2.client.registration.facebook.auth-uri}")
    private String facebookAuthUri;

    @Value("${spring.security.oauth2.client.registration.facebook.token-uri}")
    private String facebookTokenUri;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.user-info-uri}")
    private String facebookUserInfoUri;


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

        newUser.setStatus(Status.valueOf("ACTIVE"));
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
    public Page<UserResponse> getAllUsers(PageRequest pageRequest, Long roleId, String status) throws Exception {
        Specification<User> spec = Specification.where(null);

        if (roleId != null) {
            spec = spec.and(UserSpecifications.hasRole(roleId));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and(UserSpecifications.hasStatus(status));
        }


        Page<User> users = userRepository.findAll(spec, pageRequest);
        if (users.isEmpty()) {
            throw new Exception("No user found");
        }
        return users.map(user -> UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .roleName(user.getRole().getName())
                .status(user.getStatus() != null ? user.getStatus().name() : "UNKNOWN")
                .createdAt(user.getCreatedAt())
                .build());
    }

    @Override
    public List<User> findAdmins() throws Exception {
        return userRepository.findAllByRoleName(Role.ADMIN);
    }

    @Override
    public void changeStatusUser(String status, Long id) throws Exception {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setStatus(Status.valueOf(status));
            userRepository.save(user);
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public List<UserDTO> get5LatestUsers() throws Exception {
        try {
            List<User> users = userRepository.findTop5ByOrderByCreatedAtDesc();
                return users.stream().map(user -> UserDTO.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .roleId(user.getRole().getId())
                        .status(user.getStatus() != null ? user.getStatus().name() : "UNKNOWN")
                        .roleName(user.getRole().getName())
                        .build()).toList();
        } catch (Exception e) {
            throw new Exception("Failed to get latest users");
        }
    }

    @Override
    public String generateAuthUrl(String loginType) {
        String url = "";
        loginType = loginType.trim().toLowerCase();
        if (loginType.equals("google")) {
            GoogleAuthorizationCodeRequestUrl urlBuilder = new GoogleAuthorizationCodeRequestUrl(
                    googleClientId,
                    googleRedirectUri,
                    Arrays.asList("email", "profile", "openid"));
            url = urlBuilder.build();
        } else if (loginType.equals("facebook")) {
            url = UriComponentsBuilder
                    .fromUriString(facebookAuthUri)
                    .queryParam("client_id", facebookClientId)
                    .queryParam("redirect_uri", facebookRedirectUri)
                    .queryParam("scope", "email,public_profile")
                    .queryParam("response_type", "code")
                    .build()
                    .toUriString();
        }
        return url;
    }

    @Override
    public UserDTO authenticateAndFetchProfile(String code, String loginType) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        String accessToken;

        switch (loginType.toLowerCase()) {
            case "google":
                accessToken = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(), new GsonFactory(),
                        googleClientId,
                        googleClientSecret,
                        code,
                        googleRedirectUri
                ).execute().getAccessToken();

                restTemplate.getInterceptors().add((req, body, executionContext) -> {
                    req.getHeaders().set("Authorization", "Bearer " + accessToken);
                    return executionContext.execute(req, body);
                });

                String responseBody = restTemplate.getForEntity(googleUserInfoUri, String.class).getBody();

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> userInfo = objectMapper.readValue(responseBody, Map.class);

                UserDTO userDTO = new UserDTO();
                userDTO.setGoogleAccountId((String) userInfo.get("sub"));
                userDTO.setFullName((String) userInfo.get("name"));
                userDTO.setEmail((String) userInfo.get("email"));
                userDTO.setAvatarUrl((String) userInfo.get("picture"));

                return userDTO;

            case "facebook":
                String urlGetAccessToken = UriComponentsBuilder
                        .fromUriString(facebookTokenUri)
                        .queryParam("client_id", facebookClientId)
                        .queryParam("redirect_uri", facebookRedirectUri)
                        .queryParam("client_secret", facebookClientSecret)
                        .queryParam("code", code)
                        .toUriString();

                ResponseEntity<String> response = restTemplate.getForEntity(urlGetAccessToken, String.class);
                ObjectMapper facebookMapper = new ObjectMapper();
                JsonNode facebookNode = facebookMapper.readTree(response.getBody());
                accessToken = facebookNode.get("access_token").asText();

                restTemplate.getInterceptors().add((req, body, executionContext) -> {
                    req.getHeaders().set("Authorization", "Bearer " + accessToken);
                    return executionContext.execute(req, body);
                });

                String facebookResponseBody = restTemplate.getForEntity(facebookUserInfoUri, String.class).getBody();
                Map<String, Object> facebookUserInfo = facebookMapper.readValue(facebookResponseBody, Map.class);

                UserDTO facebookUserDTO = new UserDTO();
                facebookUserDTO.setGoogleAccountId((String) facebookUserInfo.get("id")); // Facebook user ID
                facebookUserDTO.setFullName((String) facebookUserInfo.get("name"));
                facebookUserDTO.setEmail((String) facebookUserInfo.get("email"));

                if (facebookUserInfo.containsKey("picture")) {
                    Map<String, Object> pictureData = (Map<String, Object>) facebookUserInfo.get("picture");
                    if (pictureData.containsKey("data")) {
                        Map<String, Object> pictureUrl = (Map<String, Object>) pictureData.get("data");
                        facebookUserDTO.setAvatarUrl((String) pictureUrl.get("url"));
                    }
                }

                return facebookUserDTO;

            default:
                System.out.println("Unsupported login type: " + loginType);
                return null;
        }
    }

    @Override
    public String loginGoogle(UserDTO userDTO) throws Exception {
        Optional<User> optionalUser = userRepository.findByGoogleAccountId(userDTO.getGoogleAccountId());
        Role role = roleRepository.findById(
                userDTO.getRoleId()).orElseThrow(() -> new Exception("Role not found"));
        Optional<User> existingUser =userRepository.findByEmail(userDTO.getEmail());
        if (userDTO.isGoogleAccountIdValid()) {
            if (optionalUser.isEmpty() && existingUser.isEmpty()) {
                User newUser = User.builder()
                        .fullName(Optional.ofNullable(userDTO.getFullName()).orElse(""))
                        .email(Optional.ofNullable(userDTO.getEmail()).orElse(""))
                        .avatarUrl(Optional.ofNullable(userDTO.getAvatarUrl()).orElse(""))
                        .role(role)
                        .googleAccountId(userDTO.getGoogleAccountId())
                        .password("") // Mật khẩu trống cho đăng nhập mạng xã hội
                        .enabled(true)
                        .status(Status.ACTIVE)
                        .build();

                newUser = userRepository.save(newUser);
                optionalUser = Optional.of(newUser);
            } else if (existingUser.isPresent()) {
                User user = existingUser.get();
                user.setGoogleAccountId(userDTO.getGoogleAccountId());
                userRepository.save(user);
                return this.login(user.getEmail(), "", user.getRole().getId());
            }
        } else {
            throw new IllegalArgumentException("Invalid social account information.");
        }
        User user = optionalUser.get();

        return this.login(user.getEmail(), "", user.getRole().getId());
    }

    @Override
    public void contactAdmin(EmailRequestDTO emailRequestDTO) throws Exception {
        String htmlMessage = generateHtmlMessage(emailRequestDTO);
        try {
            List<User> admins = userRepository.findAllByRoleName(Role.ADMIN);
            for (User admin : admins) {
                emailService.sendEmail(admin.getEmail(), emailRequestDTO.getSubject(), htmlMessage);
            }
        } catch (MessagingException e) {
            throw new Exception("Failed to send email");
        }
    }

    @Override
    public String loginFacebook(UserDTO userDTO) throws Exception {
        Optional<User> optionalUser = userRepository.findByFacebookAccountId(userDTO.getGoogleAccountId());
        Role role = roleRepository.findById(
                userDTO.getRoleId()).orElseThrow(() -> new Exception("Role not found"));
        Optional<User> existingUser =userRepository.findByEmail(userDTO.getEmail());
        if (userDTO.isFacebookAccountIdValid()) {
            if (optionalUser.isEmpty() && existingUser.isEmpty()) {
                User newUser = User.builder()
                        .fullName(Optional.ofNullable(userDTO.getFullName()).orElse(""))
                        .email(Optional.ofNullable(userDTO.getEmail()).orElse(""))
                        .avatarUrl(Optional.ofNullable(userDTO.getAvatarUrl()).orElse(""))
                        .role(role)
                        .facebookAccountId(userDTO.getFacebookAccountId())
                        .password("") // Mật khẩu trống cho đăng nhập mạng xã hội
                        .enabled(true)
                        .status(Status.ACTIVE)
                        .build();

                newUser = userRepository.save(newUser);
                optionalUser = Optional.of(newUser);
            } else if (existingUser.isPresent()) {
                User user = existingUser.get();
                user.setFacebookAccountId(userDTO.getFacebookAccountId());
                userRepository.save(user);
                return this.login(user.getEmail(), "", user.getRole().getId());
            }
        } else {
            throw new IllegalArgumentException("Invalid social account information.");
        }
        User user = optionalUser.get();

        return this.login(user.getEmail(), "", user.getRole().getId());
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    private String generateHtmlMessage(EmailRequestDTO emailRequestDTO) {
        return "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px; border-radius: 5px;\">"
                + "<h2 style=\"color: #333; text-align: center;\">You have a new message from " + emailRequestDTO.getEmail() + "</h2>"
                + "<p style=\"font-size: 16px;\">"
                + "<strong>Subject: </strong>" + emailRequestDTO.getSubject() + "<br><br>"
                + "<strong>Message: </strong><br>" + emailRequestDTO.getContent()
                + "</p>"
                + "</div>"
                + "</body>"
                + "</html>";
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
            if (!Objects.equals(existingUser.getRole().getId(), roleId)) {
                throw new Exception("Invalid role");
            }

            if (existingUser.getGoogleAccountId() != null) {
                /*UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
                authenticationManager.authenticate(authenticationToken);*/
                return jwtUtil.generateToken(existingUser);
            }

            if (!passwordEncoder.matches(password, existingUser.getPassword())) {
                throw new Exception("Invalid phone number or password");
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
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
    public UserDTO findUserDTOById(Long id) throws Exception {
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("User not found"));
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .gender(user.getGender() != null ? user.getGender() : "")
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .roleId(user.getRole().getId())
                .status(user.getStatus() != null ? user.getStatus().name() : "UNKNOWN")
                .roleName(user.getRole().getName())
                .build();
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
                .status(user.getStatus() != null ? user.getStatus().name() : "UNKNOWN")
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
    public User updateAvatar(String username, String avatarUrl) throws Exception {
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
    public User updateProfile(String username, ProfileDTO profileDTO) throws Exception {
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
