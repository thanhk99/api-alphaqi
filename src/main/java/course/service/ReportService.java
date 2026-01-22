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

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public ReportPagedResponse getAllReports(ReportType type, Pageable pageable) {
        Page<Report> reports;
        LocalDateTime latestUpdatedAt;

        if (type != null) {
            reports = reportRepository.findByType(type, pageable);
            latestUpdatedAt = reportRepository.findLatestUpdatedAtByType(type);
        } else {
            reports = reportRepository.findAll(pageable);
            latestUpdatedAt = reportRepository.findLatestUpdatedAt();
        }

        return ReportPagedResponse.builder()
                .reports(reports.map(this::convertToResponse))
                .latestUpdatedAt(latestUpdatedAt)
                .build();
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

        Report report = Report.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .pdfUrl(pdfUrl)
                .externalLink(request.getExternalLink())
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
        if (pdfUrl != null) {
            report.setPdfUrl(pdfUrl);
        }
        report.setExternalLink(request.getExternalLink());

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
                .externalLink(report.getExternalLink())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
