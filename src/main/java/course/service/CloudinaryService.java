package course.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import course.dto.UploadResponse;
import course.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024; // 500MB
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_VIDEO_FORMATS = { "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm" };
    private static final String[] ALLOWED_IMAGE_FORMATS = { "jpg", "jpeg", "png", "gif", "webp" };

    public UploadResponse uploadVideo(MultipartFile file) {
        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 500MB");
        }

        // Check file type
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidVideoFormat(originalFilename)) {
            throw new BadRequestException("Invalid video format. Allowed formats: mp4, avi, mov, wmv, flv, mkv, webm");
        }

        try {
            // Upload to Cloudinary
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "video",
                    "folder", "course-videos",
                    "overwrite", true,
                    "quality", "auto");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            // Build response
            return UploadResponse.builder()
                    .url((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .format((String) uploadResult.get("format"))
                    .bytes(((Number) uploadResult.get("bytes")).longValue())
                    .duration(uploadResult.get("duration") != null ? ((Number) uploadResult.get("duration")).intValue()
                            : null)
                    .build();

        } catch (IOException e) {
            log.error("Error uploading video to Cloudinary", e);
            throw new BadRequestException("Failed to upload video: " + e.getMessage());
        }
    }

    public UploadResponse uploadImage(MultipartFile file) {
        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 10MB");
        }

        // Check file type
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidImageFormat(originalFilename)) {
            throw new BadRequestException("Invalid image format. Allowed formats: jpg, jpeg, png, gif, webp");
        }

        try {
            // Upload to Cloudinary
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", "course-thumbnails",
                    "overwrite", true,
                    "quality", "auto");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            // Build response
            return UploadResponse.builder()
                    .url((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .format((String) uploadResult.get("format"))
                    .bytes(((Number) uploadResult.get("bytes")).longValue())
                    .build();

        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary", e);
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }

    private boolean isValidVideoFormat(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        for (String format : ALLOWED_VIDEO_FORMATS) {
            if (format.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidImageFormat(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        for (String format : ALLOWED_IMAGE_FORMATS) {
            if (format.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
