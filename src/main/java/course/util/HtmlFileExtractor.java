package course.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Trích xuất nội dung HTML từ file đầy đủ thành dạng embeddable.
 * Dùng khi frontend inject bằng innerHTML thay vì iframe.
 */
public class HtmlFileExtractor {

    /**
     * Parse file HTML upload lên, trích nội dung embeddable:
     * - Lấy toàn bộ <style> từ <head>
     * - Lấy toàn bộ nội dung trong <body>
     * - Bọc trong wrapper div để scoped
     * - Giữ nguyên <script> (sẽ được frontend re-execute)
     */
    public static String extractEmbeddable(MultipartFile htmlFile) throws IOException {
        if (htmlFile == null || htmlFile.isEmpty()) {
            return null;
        }

        // Đọc nội dung file
        String rawHtml = new String(htmlFile.getBytes(), StandardCharsets.UTF_8);

        // Parse bằng Jsoup
        Document doc = Jsoup.parse(rawHtml);
        doc.outputSettings().prettyPrint(false);

        StringBuilder embeddable = new StringBuilder();

        // --- Trích <style> từ <head> ---
        Elements styleElements = doc.head().select("style");
        if (!styleElements.isEmpty()) {
            embeddable.append("<style>\n");
            for (Element style : styleElements) {
                embeddable.append(style.data()).append("\n");
            }
            embeddable.append("</style>\n");
        }

        // --- Trích <link rel="stylesheet"> từ <head> ---
        Elements linkElements = doc.head().select("link[rel=stylesheet]");
        for (Element link : linkElements) {
            embeddable.append(link.outerHtml()).append("\n");
        }

        // --- Trích nội dung <body> ---
        Element body = doc.body();
        if (body != null) {
            embeddable.append(body.html());
        }

        return embeddable.toString();
    }

    /**
     * Parse từ chuỗi HTML thô (dùng khi lưu content dạng text).
     */
    public static String extractEmbeddableFromString(String rawHtml) {
        if (rawHtml == null || rawHtml.isBlank()) {
            return null;
        }

        Document doc = Jsoup.parse(rawHtml);
        doc.outputSettings().prettyPrint(false);

        StringBuilder embeddable = new StringBuilder();

        Elements styleElements = doc.head().select("style");
        if (!styleElements.isEmpty()) {
            embeddable.append("<style>\n");
            for (Element style : styleElements) {
                embeddable.append(style.data()).append("\n");
            }
            embeddable.append("</style>\n");
        }

        Elements linkElements = doc.head().select("link[rel=stylesheet]");
        for (Element link : linkElements) {
            embeddable.append(link.outerHtml()).append("\n");
        }

        Element body = doc.body();
        if (body != null) {
            embeddable.append(body.html());
        }

        return embeddable.toString();
    }
}
