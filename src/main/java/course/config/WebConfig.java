package course.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StringToReportTypeConverter stringToReportTypeConverter;

    public WebConfig(StringToReportTypeConverter stringToReportTypeConverter) {
        this.stringToReportTypeConverter = stringToReportTypeConverter;
    }

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(stringToReportTypeConverter);
    }
}
