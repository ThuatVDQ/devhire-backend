package com.hcmute.devhire.services;


import com.hcmute.devhire.DTOs.UserDTO;
import com.hcmute.devhire.entities.Role;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.RoleRepository;
import com.hcmute.devhire.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.hcmute.devhire.components.JwtUtils;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws Exception {
        String phone= userDTO.getPhone();

        if(userRepository.existByPhoneNumber(phone)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        Role role = roleRepository.findById(
                userDTO.getRoleId()).orElseThrow(() -> new Exception("Role not found"));
        if (role.getName().toUpperCase().equals(Role.ADMIN)) {
            throw new Exception("Cannot create admin user");
        }
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phone(userDTO.getPhone())
                .password(userDTO.getPassword())
                .build();

        newUser.setRole(role);

        String password = userDTO.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword);

        return userRepository.save(newUser);
    }

    @Override
    public String login(String phone, String password, Long roleId) throws Exception {
        Optional<User> user = userRepository.findByPhone(phone);
        if (user.isEmpty()) {
            throw new Exception("Invalid phone number or password");
        }
        User existingUser = user.get();
        //check password

        if (!passwordEncoder.matches(password, existingUser.getPassword())) {
            throw new Exception("Invalid phone number or password");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(phone, password);
        //authenticate with java spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtUtils.generateToken(user.get());
    }
}
