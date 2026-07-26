package net.engineerAnsh.journalApp.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Dto.admin.CreateAdminDto;
import net.engineerAnsh.journalApp.Dto.auth.AuthResponseDto;
import net.engineerAnsh.journalApp.Dto.user.*;
import net.engineerAnsh.journalApp.Entity.Journal;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.JournalRepository;
import net.engineerAnsh.journalApp.Repository.UserRepository;
import net.engineerAnsh.journalApp.Utils.JwtUtils;
import net.engineerAnsh.journalApp.api.responses.WeatherResponse;
import net.engineerAnsh.journalApp.enums.Role;
import net.engineerAnsh.journalApp.exception.exceptions.DuplicateResourceException;
import net.engineerAnsh.journalApp.exception.exceptions.ResourceNotFoundException;
import net.engineerAnsh.journalApp.exception.exceptions.BadRequestException;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JournalRepository journalRepository;
    private final WeatherService weatherService;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final JwtUtils jwtUtils;
    private static final String USERNAME_CHANGED = "Username updated successfully.";

    private String getLoggedInUser() {
        Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication(); // Again, 'SecurityContextHolder.getContext().getAuthentication() '-> gives the current logged-in user...
        return userAuthenticated.getName();
    }

    private UserProfileResponseDto mapToUserProfileResponse(User user) {
        return new UserProfileResponseDto(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getCity(),
                user.isSentimentAnalysis(),
                user.getRoles(),
                user.getProfileImageUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private AuthResponseDto mapToAuthResponse(String token) {
        return new AuthResponseDto(
                UserService.USERNAME_CHANGED,
                token,
                "Bearer"
        );
    }

    public void saveEntry(User user) {
        userRepository.save(user);
    }

    public List<UserProfileResponseDto> getAll() {
        return userRepository
                .findAll()
                .stream()
                .map(this::mapToUserProfileResponse)
                .toList();
    }

    public User findUserByUserName(String userName) {
        return userRepository.findByUsername(userName);
    }

    @Transactional
    public void saveAdmin(CreateAdminDto request) {
        User user = findUserByUserName(request.getUsername());
        if (user == null) throw new ResourceNotFoundException("User not Found");

        if (!user.getEmail().equals(request.getEmail())) {
            throw new BadRequestException("User email is wrong");
        }

        if (user.getRoles().contains(Role.ADMIN)) {
            throw new BadRequestException("User is already admin...");
        }

        user.getRoles().add(Role.ADMIN);
        userRepository.save(user);
    }

    public void changePassword(ChangePasswordRequestDto request) {

        String userName = getLoggedInUser();
        User user = findUserByUserName(userName); // getting the user by its userName...

        if (user == null) throw new ResourceNotFoundException("User not Found");

        // Verify current password
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new BadRequestException("Current password is incorrect.");
        }

        // Check new password confirmation
        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new BadRequestException("New password and confirm password do not match.");
        }

        // Prevent same password
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new BadRequestException("New password cannot be the same as the current password.");
        }

        // Encode and save
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }

    public void changeEmail(ChangeEmailRequestDto request) {

        String userName = getLoggedInUser();
        User user = findUserByUserName(userName); // getting the user by its userName...

        if (user == null) throw new ResourceNotFoundException("User not Found");

        // Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new BadRequestException("Password is incorrect.");
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(request.getNewEmail()))
            throw new DuplicateResourceException(
                    "newEmail",
                    "Email already exists."
            );

        // Prevent updating to same email
        if (user.getEmail().equalsIgnoreCase(request.getNewEmail())) {
            throw new BadRequestException("New email cannot be the same as the current email.");
        }

        user.setEmail(request.getNewEmail());

        userRepository.save(user);
    }

    public AuthResponseDto changeUsername(ChangeUsernameRequestDto request) {

        String userName = getLoggedInUser();
        User user = findUserByUserName(userName); // getting the user by its userName...

        if (user == null) throw new ResourceNotFoundException("User not Found");

        String newUsername = request.getUsername();

        // Prevent updating to the same username
        if (user.getUsername().equalsIgnoreCase(newUsername)) {
            throw new BadRequestException("New username cannot be the same as the current username.");
        }

        // Check username availability
        if (userRepository.existsByUsername(newUsername)) {
            throw new DuplicateResourceException(
                    "username",
                    "Username is already taken."
            );
        }

        user.setUsername(newUsername);
        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getUsername());
        return mapToAuthResponse(token);

    }

    public void deleteTheUser(DeleteAccountRequestDto request) {
        String userName = getLoggedInUser();
        User user = findUserByUserName(userName);
        if (user == null) throw new ResourceNotFoundException("User not Found");

        // Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new BadRequestException("Password is incorrect.");
        }

        // collect all the id's of the journal, and then delete all the journals corresponding to these id's...
        List<ObjectId> journalEntriesIds = user.getJournals()
                .stream()
                .map(Journal::getId)
                .toList();

        journalRepository.deleteAllById(journalEntriesIds);

        userRepository.deleteByUsername(userName);// Deletes the user directly from MongoDB using their username...
    }

    @Transactional
    public void updateUser(UpdateProfileRequestDto request) {
        String userName = getLoggedInUser();
        User savedUser = findUserByUserName(userName); // getting the user by its userName...

        if (savedUser == null) throw new ResourceNotFoundException("User not Found");

        if (request.getCity() != null && !request.getCity().isEmpty()) {
            savedUser.setCity(request.getCity());
        }

        if (request.isSentimentAnalysisEnabled() != savedUser.isSentimentAnalysis()) {
            savedUser.setSentimentAnalysis(request.isSentimentAnalysisEnabled());
        }

        userRepository.save(savedUser);
    }

    public String greetTheUser() {
        String name = getLoggedInUser();
        User user = findUserByUserName(name);
        if (user == null) throw new ResourceNotFoundException("User not Found");

        String city = user.getCity();
        WeatherResponse weatherResponse = weatherService.getWeather(city);
        String greeting = "";
        if (weatherResponse != null) {
            greeting = "Hi " + name + " weather feels like " + weatherResponse.getCurrent().getFeelsLike() + " in " + city;
        }
        return greeting;
    }

    public UserProfileResponseDto getUser() {
        String username = getLoggedInUser();
        return mapToUserProfileResponse(findUserByUserName(username));
    }

    @Transactional
    public ProfileImageResponseDto uploadProfileImage(MultipartFile image) {

        // Get the currently authenticated user
        String username = getLoggedInUser();

        User user = findUserByUserName(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }

        // Keep the old public id before replacing it
        String oldPublicId = user.getProfileImagePublicId();

        // Upload the new image
        ImageUploadResponse uploadResponse =
                cloudinaryService.uploadProfileImage(image);

        // Update user with the new image details
        user.setProfileImageUrl(uploadResponse.getImageUrl());
        user.setProfileImagePublicId(uploadResponse.getPublicId());

        // Save the updated user
        saveEntry(user);

        // Delete the previous image after everything else succeeds
        if (oldPublicId != null && !oldPublicId.isBlank()) {
            try {
                cloudinaryService.deleteImage(oldPublicId);
            } catch (Exception ex) {
                // Log the exception.
                // We don't want the whole request to fail because
                // deleting the old image was unsuccessful.
                log.warn("Failed to delete old Cloudinary image: {}", oldPublicId, ex);
            }
        }

        return ProfileImageResponseDto.builder()
                .message("Profile image updated successfully.")
                .profileImageUrl(uploadResponse.getImageUrl())
                .build();
    }
}