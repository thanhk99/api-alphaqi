package course.controller;

import course.dto.CourseRequest;
import course.dto.CourseResponse;
import course.service.CourseService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @ModelAttribute @Valid CourseRequest request,
            @RequestPart(value = "introVideo", required = false) MultipartFile introVideo,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        CourseResponse response = courseService.createCourse(request, introVideo, thumbnail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", response));
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable String id,
            @ModelAttribute @Valid CourseRequest request,
            @RequestPart(value = "introVideo", required = false) MultipartFile introVideo,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        CourseResponse response = courseService.updateCourse(id, request, introVideo, thumbnail);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteCourse(@PathVariable String id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable String id) {
        CourseResponse response = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses(
            @RequestParam(required = false) Boolean published) {
        List<CourseResponse> courses = published != null && published
                ? courseService.getPublishedCourses()
                : courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> searchCourses(
            @RequestParam String keyword) {
        List<CourseResponse> courses = courseService.searchCourses(keyword);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> filterCourses(
            @RequestParam(required = false) String category) {
        List<CourseResponse> courses = courseService.filterCourses(category);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getHomeCourses() {
        List<CourseResponse> courses = courseService.getHomeCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

}
