package course.service;

import course.dto.PartnerCompanyRequest;
import course.dto.PartnerCompanyResponse;
import course.dto.UploadResponse;
import course.exception.ResourceNotFoundException;
import course.model.PartnerCompany;
import course.repository.PartnerCompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PartnerCompanyService {

    private final PartnerCompanyRepository partnerCompanyRepository;
    private final CloudinaryService cloudinaryService;

    public PartnerCompanyService(PartnerCompanyRepository partnerCompanyRepository,
            CloudinaryService cloudinaryService) {
        this.partnerCompanyRepository = partnerCompanyRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public PartnerCompanyResponse createPartnerCompany(PartnerCompanyRequest request, MultipartFile logo) {
        PartnerCompany company = new PartnerCompany();
        company.setName(request.getName());

        if (logo != null && !logo.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(logo);
            company.setLogo(uploadResponse.getUrl());
        } else {
            company.setLogo(request.getLogoUrl());
        }

        company = partnerCompanyRepository.save(company);
        return mapToResponse(company);
    }

    public List<PartnerCompanyResponse> getAllPartnerCompanies() {
        return partnerCompanyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PartnerCompanyResponse getPartnerCompanyById(String id) {
        PartnerCompany company = partnerCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner Company not found"));
        return mapToResponse(company);
    }

    @Transactional
    public PartnerCompanyResponse updatePartnerCompany(String id, PartnerCompanyRequest request, MultipartFile logo) {
        PartnerCompany company = partnerCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner Company not found"));

        company.setName(request.getName());

        if (logo != null && !logo.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(logo);
            company.setLogo(uploadResponse.getUrl());
        } else if (request.getLogoUrl() != null) {
            company.setLogo(request.getLogoUrl());
        }

        company = partnerCompanyRepository.save(company);
        return mapToResponse(company);
    }

    @Transactional
    public void deletePartnerCompany(String id) {
        if (!partnerCompanyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Partner Company not found");
        }
        partnerCompanyRepository.deleteById(id);
    }

    private PartnerCompanyResponse mapToResponse(PartnerCompany company) {
        return PartnerCompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logo(company.getLogo())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
