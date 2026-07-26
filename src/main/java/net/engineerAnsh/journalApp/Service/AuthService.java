package net.engineerAnsh.journalApp.Service;

import net.engineerAnsh.journalApp.Dto.auth.LoginRequestDto;
import net.engineerAnsh.journalApp.Dto.auth.LoginResponseDto;
import net.engineerAnsh.journalApp.Dto.auth.RegisterRequestDto;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.UserRepository;
import net.engineerAnsh.journalApp.Utils.JwtUtils;
import net.engineerAnsh.journalApp.enums.Role;
import net.engineerAnsh.journalApp.exception.exceptions.DuplicateResourceException;
import net.engineerAnsh.journalApp.exception.exceptions.ResourceNotFoundException;
import net.engineerAnsh.journalApp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void signup(RegisterRequestDto request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "username",
                    "Username already exists."
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "email",
                    "Email already exists."
            );
        }

        // We are creating the newUser(obj. of User-entity), and fetching details from the UserProfileDto object...
        // Then saving this newUser...
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setCity(request.getCity());
        newUser.setEmail(request.getEmail());
        newUser.setSentimentAnalysis(request.isSentimentAnalysisEnabled());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRoles(List.of(Role.USER));
        newUser.setJournals(new ArrayList<>());
        userRepository.save(newUser);
    }

    public LoginResponseDto login(LoginRequestDto request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userService.findUserByUserName(request.getUsername());

        if (user == null) {
            throw new ResourceNotFoundException("Username or Password incorrect.");
        }

        String token = jwtUtils.generateToken(user.getUsername());

        return LoginResponseDto.builder()
                .accessToken(token)
                .user(userMapper.toSummaryDto(user))
                .build();
    }
}
