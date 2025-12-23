package course.controller;

import course.dto.PartnerCompanyRequest;
import course.dto.PartnerCompanyResponse;
import course.service.PartnerCompanyService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/partner-companies")
public class PartnerCompanyController {

    private final PartnerCompanyService partnerCompanyService;

    public PartnerCompanyController(PartnerCompanyService partnerCompanyService) {
        this.partnerCompanyService = partnerCompanyService;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PartnerCompanyResponse>> createPartnerCompany(
            @ModelAttribute @Valid PartnerCompanyRequest request,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {
        PartnerCompanyResponse response = partnerCompanyService.createPartnerCompany(request, logo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Partner Company created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PartnerCompanyResponse>>> getAllPartnerCompanies() {
        List<PartnerCompanyResponse> responses = partnerCompanyService.getAllPartnerCompanies();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerCompanyResponse>> getPartnerCompanyById(@PathVariable String id) {
        PartnerCompanyResponse response = partnerCompanyService.getPartnerCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PartnerCompanyResponse>> updatePartnerCompany(
            @PathVariable String id,
            @ModelAttribute @Valid PartnerCompanyRequest request,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {
        PartnerCompanyResponse response = partnerCompanyService.updatePartnerCompany(id, request, logo);
        return ResponseEntity.ok(ApiResponse.success("Partner Company updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePartnerCompany(@PathVariable String id) {
        partnerCompanyService.deletePartnerCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Partner Company deleted successfully", null));
    }
}
