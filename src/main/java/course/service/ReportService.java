package course.service;

import course.dto.ReportRequest;
import course.dto.ReportResponse;
import course.dto.ReportPagedResponse;
import course.dto.UploadResponse;
import course.exception.ResourceNotFoundException;
import course.model.Report;
import course.model.enums.ReportType;
import course.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final CloudinaryService cloudinaryService;

    public List<Map<String, Object>> getReportTypes() {
        return Arrays.stream(ReportType.values())
                .map(type -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("code", type.name());
                    map.put("displayName", type.getDisplayName());
                    map.put("parentCode", type.getParent() != null ? type.getParent().name() : null);
                    map.put("isParent", Arrays.stream(ReportType.values()).anyMatch(t -> t.getParent() == type));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportPagedResponse getAllReports(ReportType type, String search, Pageable pageable) {
        Page<Report> reports;
        LocalDateTime latestUpdatedAt;

        if (type != null) {
            List<ReportType> types = getTypesIncludingChildren(type);
            if (search != null && !search.isBlank()) {
                reports = reportRepository.findByTypeInAndTitleContainingIgnoreCase(types, search, pageable);
            } else {
                reports = reportRepository.findByTypeIn(types, pageable);
            }
            latestUpdatedAt = reportRepository.findLatestUpdatedAtByType(type);
        } else {
            if (search != null && !search.isBlank()) {
                reports = reportRepository.findByTitleContainingIgnoreCase(search, pageable);
            } else {
                reports = reportRepository.findAll(pageable);
            }
            latestUpdatedAt = reportRepository.findLatestUpdatedAt();
        }

        return ReportPagedResponse.builder()
                .reports(reports.map(this::convertToResponse))
                .latestUpdatedAt(latestUpdatedAt)
                .build();
    }

    private List<ReportType> getTypesIncludingChildren(ReportType type) {
        List<ReportType> types = new java.util.ArrayList<>();
        types.add(type);
        types.addAll(Arrays.stream(ReportType.values())
                .filter(t -> t.getParent() == type)
                .collect(Collectors.toList()));
        return types;
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return convertToResponse(report);
    }

    @Transactional
    public ReportResponse createReport(ReportRequest request, MultipartFile file) {
        String pdfUrl = request.getPdfUrl();

        if (file != null && !file.isEmpty()) {
            UploadResponse uploadResult = cloudinaryService.uploadPDF(file, "reports");
            pdfUrl = uploadResult.getUrl();
        }

        if (pdfUrl == null || pdfUrl.isBlank()) {
            throw new IllegalArgumentException("Report PDF URL or file is required");
        }

        Report report = Report.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .pdfUrl(pdfUrl)
                .externalUrl(request.getExternalUrl())
                .build();

        return convertToResponse(reportRepository.save(report));
    }

    @Transactional
    public ReportResponse updateReport(Long id, ReportRequest request, MultipartFile file) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        String pdfUrl = request.getPdfUrl();
        if (file != null && !file.isEmpty()) {
            UploadResponse uploadResult = cloudinaryService.uploadPDF(file, "reports");
            pdfUrl = uploadResult.getUrl();
        }

        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setType(request.getType());
        if (pdfUrl != null && !pdfUrl.isBlank()) {
            report.setPdfUrl(pdfUrl);
        }
        report.setExternalUrl(request.getExternalUrl());

        return convertToResponse(reportRepository.save(report));
    }

    @Transactional
    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report not found with id: " + id);
        }
        reportRepository.deleteById(id);
    }

    private ReportResponse convertToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .title(report.getTitle())
                .description(report.getDescription())
                .type(report.getType())
                .typeDisplayName(report.getType() != null ? report.getType().getDisplayName() : null)
                .pdfUrl(report.getPdfUrl())
                .externalUrl(report.getExternalUrl())
                .parentType(report.getType() != null ? report.getType().getParent() : null)
                .parentTypeDisplayName(report.getType() != null && report.getType().getParent() != null
                        ? report.getType().getParent().getDisplayName()
                        : null)
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
