package course.config;

import course.model.enums.ReportType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToReportTypeConverter implements Converter<String, ReportType> {
    @Override
    public ReportType convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            return ReportType.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ReportType: " + source);
        }
    }
}
