package course.service;

import course.dto.AdminRequest;
import course.dto.AdminResponse;
import course.exception.BadRequestException;
import course.exception.ResourceNotFoundException;
import course.model.Admin;
import course.model.enums.AccountStatus;
import course.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AdminResponse createAdmin(AdminRequest request) {
        // Check if username exists
        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // Check if email exists
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setFullName(request.getFullName());
        admin.setPhoneNumber(request.getPhoneNumber());

        admin = adminRepository.save(admin);
        return mapToResponse(admin);
    }

    @Transactional
    public AdminResponse updateAdmin(String id, AdminRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id));

        admin.setFullName(request.getFullName());
        admin.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        admin = adminRepository.save(admin);
        return mapToResponse(admin);
    }

    @Transactional
    public AdminResponse lockAdmin(String id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id));

        admin.setStatus(AccountStatus.LOCKED);
        admin = adminRepository.save(admin);

        return mapToResponse(admin);
    }

    @Transactional
    public AdminResponse unlockAdmin(String id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id));

        admin.setStatus(AccountStatus.ACTIVE);
        admin = adminRepository.save(admin);

        return mapToResponse(admin);
    }

    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AdminResponse mapToResponse(Admin admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .fullName(admin.getFullName())
                .phoneNumber(admin.getPhoneNumber())
                .status(admin.getStatus().name())
                .createdAt(admin.getCreatedAt())
                .updatedAt(admin.getUpdatedAt())
                .build();
    }
}
