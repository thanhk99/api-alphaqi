package course.service;

import course.dto.ChangePasswordRequest;
import course.dto.UpdateProfileRequest;
import course.dto.UserResponse;
import course.exception.BadRequestException;
import course.exception.ResourceNotFoundException;
import course.model.User;
import course.model.enums.AccountStatus;
import course.model.enums.MembershipLevel;
import course.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return mapToResponse(user);
    }

    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(search, search,
                    pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::mapToResponse);
    }

    @Transactional
    public UserResponse upgradeMembership(String username, String level) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        try {
            MembershipLevel membershipLevel = MembershipLevel.valueOf(level.toUpperCase());
            user.setMembershipLevel(membershipLevel);
            user = userRepository.save(user);

            return mapToResponse(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid membership level: " + level);
        }
    }

    @Transactional
    public UserResponse lockUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        user.setStatus(AccountStatus.LOCKED);
        user = userRepository.save(user);

        return mapToResponse(user);
    }

    @Transactional
    public UserResponse unlockUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        user.setStatus(AccountStatus.ACTIVE);
        user = userRepository.save(user);

        return mapToResponse(user);
    }

    @Transactional
    public UserResponse deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (user.getDeletedAt() != null) {
            throw new IllegalStateException("User is already deleted");
        }

        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(AccountStatus.LOCKED);
        user = userRepository.save(user);

        return mapToResponse(user);
    }

    @Transactional
    public UserResponse restoreUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (user.getDeletedAt() == null) {
            throw new IllegalStateException("User is not deleted");
        }

        user.setDeletedAt(null);
        user.setStatus(AccountStatus.ACTIVE);
        user = userRepository.save(user);

        return mapToResponse(user);
    }

    public Page<UserResponse> getDeletedUsers(String search, Pageable pageable) {
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByStatusAndFullNameContainingIgnoreCaseOrStatusAndUsernameContainingIgnoreCase(
                    AccountStatus.LOCKED, search, AccountStatus.LOCKED, search, pageable);
        } else {
            users = userRepository.findByStatus(AccountStatus.LOCKED, pageable);
        }

        return users.map(this::mapToResponse);
    }

    public Page<UserResponse> getActiveUsers(String search, Pageable pageable) {
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByStatusAndFullNameContainingIgnoreCaseOrStatusAndUsernameContainingIgnoreCase(
                    AccountStatus.ACTIVE, search, AccountStatus.ACTIVE, search, pageable);
        } else {
            users = userRepository.findByStatus(AccountStatus.ACTIVE, pageable);
        }

        return users.map(this::mapToResponse);
    }

    // User profile management methods
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Verify new password is different from current password
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update full name if provided
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }

        // Update email if provided and different from current
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equals(user.getEmail())) {
                // Check if email is already taken by another user
                userRepository.findByEmail(newEmail).ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(userId)) {
                        throw new BadRequestException("Email is already in use");
                    }
                });
                user.setEmail(newEmail);
            }
        }

        // Update phone number if provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUserAvatar(String userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setAvatar(avatarUrl);
        user = userRepository.save(user);

        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatar(user.getAvatar())
                .membershipLevel(user.getMembershipLevel().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}
