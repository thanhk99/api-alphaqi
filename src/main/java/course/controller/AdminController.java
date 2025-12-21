package course.controller;

import course.dto.AdminRequest;
import course.dto.AdminResponse;
import course.service.AdminService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admins")

public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(@Valid @RequestBody AdminRequest request) {
        AdminResponse response = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(
            @PathVariable String id,
            @Valid @RequestBody AdminRequest request) {
        AdminResponse response = adminService.updateAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Admin updated successfully", response));
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<AdminResponse>> lockAdmin(@PathVariable String id) {
        AdminResponse response = adminService.lockAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Admin locked successfully", response));
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<AdminResponse>> unlockAdmin(@PathVariable String id) {
        AdminResponse response = adminService.unlockAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Admin unlocked successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getAllAdmins() {
        List<AdminResponse> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(ApiResponse.success(admins));
    }
}
