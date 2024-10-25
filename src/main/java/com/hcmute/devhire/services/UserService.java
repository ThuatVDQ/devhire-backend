package com.hcmute.devhire.services;


import com.hcmute.devhire.DTOs.UserDTO;
import com.hcmute.devhire.entities.Role;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.RoleRepository;
import com.hcmute.devhire.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.hcmute.devhire.components.JwtUtil;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
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
                .password(userDTO.getPassword())
                .build();

        newUser.setRole(role);

        String password = userDTO.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword);

        return userRepository.save(newUser);
    }

    @Override
    public String login(String username, String password, Long roleId) throws Exception {
        Optional<User> user;
        if (username.contains("@")) {
            user = Optional.ofNullable(userRepository.findByEmail(username)
                    .orElseThrow(() -> new Exception("Email not found")));
        } else {
            user = Optional.ofNullable(userRepository.findByPhone(username)
                    .orElseThrow(() -> new Exception("Phone number not found")));
        }

        User existingUser = user.get();

        if (!passwordEncoder.matches(password, existingUser.getPassword())) {
            throw new Exception("Invalid phone number or password");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        //authenticate with java spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtUtil.generateToken(existingUser);
    }

    @Override
    public User findById(Long id) throws Exception {
        return userRepository.findById(id).orElseThrow(() -> new Exception("User not found"));
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
    public UserDTO getProfile(String phone) throws Exception {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new Exception("User not found"));
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

    private String safeGet(String value) {
        return value != null ? value : "";
    }

}
