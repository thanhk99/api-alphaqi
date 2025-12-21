package course.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class HtmlSanitizer {

    /**
     * @param html The unsafe HTML string.
     * @return A safe HTML string.
     */
    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(html, Safelist.relaxed()
                .addProtocols("img", "src", "data", "http", "https"));
    }
}
