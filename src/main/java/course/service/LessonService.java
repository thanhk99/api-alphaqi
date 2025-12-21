package course.service;

import course.dto.LessonRequest;
import course.dto.LessonResponse;
import course.exception.ResourceNotFoundException;
import course.model.Course;
import course.model.Lesson;
import course.repository.CourseRepository;
import course.repository.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    public LessonService(LessonRepository lessonRepository, CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public LessonResponse createLesson(LessonRequest request) {
        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());

        lesson = lessonRepository.save(lesson);
        return mapToResponse(lesson);
    }

    public List<LessonResponse> getAllLessons() {
        return lessonRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse getLessonById(String id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        return mapToResponse(lesson);
    }

    @Transactional
    public LessonResponse updateLesson(String id, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());

        lesson = lessonRepository.save(lesson);
        return mapToResponse(lesson);
    }

    @Transactional
    public void deleteLesson(String id) {
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson not found");
        }
        // Need to remove relationships before deleting if mappedBy is on Course side
        // and we want to be safe,
        // but since Course is the owner of ManyToMany (mappedBy is on Lesson),
        // actually Course has the JoinTable.
        // Wait, in Lesson.java: @ManyToMany(mappedBy = "lessons") private List<Course>
        // courses;
        // In Course.java: @ManyToMany @JoinTable ... private List<Lesson> lessons;
        // So Course is the owner.
        // We should remove this lesson from all courses before deleting.

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        for (Course course : lesson.getCourses()) {
            course.getLessons().remove(lesson);
            courseRepository.save(course);
        }

        lessonRepository.delete(lesson);
    }

    @Transactional
    public void addLessonToCourse(String courseId, String lessonId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        if (!course.getLessons().contains(lesson)) {
            course.getLessons().add(lesson);
            courseRepository.save(course);
        }
    }

    @Transactional
    public void removeLessonFromCourse(String courseId, String lessonId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        course.getLessons().remove(lesson);
        courseRepository.save(course);
    }

    public List<LessonResponse> getLessonsByCourseId(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        return course.getLessons().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LessonResponse mapToResponse(Lesson lesson) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
