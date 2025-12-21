package course.util;

import java.security.SecureRandom;

public class IdGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();

    public static String generateId() {
        return generateRandomAlphanumeric(ID_LENGTH);
    }

    /**
     * Generate a random 6-character alphanumeric code prefixed with KH-
     * 
     * @return KH-XXXXXX code
     */
    public static String generateCourseCode() {
        return "KH-" + generateRandomAlphanumeric(6).toUpperCase();
    }

    private static String generateRandomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
