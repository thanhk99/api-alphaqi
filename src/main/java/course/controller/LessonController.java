package course.controller;

import course.dto.LessonRequest;
import course.dto.LessonResponse;
import course.service.LessonService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(@RequestBody @Valid LessonRequest request) {
        LessonResponse response = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lesson created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getAllLessons() {
        List<LessonResponse> responses = lessonService.getAllLessons();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonResponse>> getLessonById(@PathVariable String id) {
        LessonResponse response = lessonService.getLessonById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(@PathVariable String id,
            @RequestBody @Valid LessonRequest request) {
        LessonResponse response = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lesson updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable String id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.ok(ApiResponse.success("Lesson deleted successfully", null));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getLessonsByCourseId(@PathVariable String courseId) {
        List<LessonResponse> responses = lessonService.getLessonsByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{lessonId}/course/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> addLessonToCourse(@PathVariable String lessonId,
            @PathVariable String courseId) {
        lessonService.addLessonToCourse(courseId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lesson added to course successfully", null));
    }

    @DeleteMapping("/{lessonId}/course/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeLessonFromCourse(@PathVariable String lessonId,
            @PathVariable String courseId) {
        lessonService.removeLessonFromCourse(courseId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lesson removed from course successfully", null));
    }
}
