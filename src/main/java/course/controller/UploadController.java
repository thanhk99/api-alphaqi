package course.controller;

import course.dto.UploadResponse;
import course.service.CloudinaryService;
import course.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/video")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadVideo(@RequestParam("file") MultipartFile file) {
        UploadResponse response = cloudinaryService.uploadVideo(file);
        return ResponseEntity.ok(ApiResponse.success("Video uploaded successfully", response));
    }

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(@RequestParam("file") MultipartFile file) {
        UploadResponse response = cloudinaryService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
    }
}
