package course.controller;

import course.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/avatars")
public class AvatarController {

    @GetMapping("/default")
    public ResponseEntity<ApiResponse<List<String>>> getDefaultAvatars() {
        try {
            // Danh sách tên file avatar
            List<String> avatarUrls = new ArrayList<>();
            avatarUrls.add("/avt/z7342362649610_f5243f87485288d44e9ec0d12aed6452.jpg");
            avatarUrls.add("/avt/z7342362649759_01f0497e9e07c09e7ea4acd0043dc54a.jpg");
            avatarUrls.add("/avt/z7342362660213_9bd0de1f5a2dbe47d74fa16eb7943f60.jpg");
            avatarUrls.add("/avt/z7342362660349_ca9feee2a3b053e240f49fd00e64c665.jpg");
            avatarUrls.add("/avt/z7342362664250_0a8fff6984e742c638a5f984ec1f795f.jpg");
            avatarUrls.add("/avt/z7342362668779_495dda9778482e8a59113105c76a57d0.jpg");
            avatarUrls.add("/avt/z7342362673523_9f1d5f6cba08d10be6d2e9aa22fefb6d.jpg");
            avatarUrls.add("/avt/z7342362677823_e9e9ab6189d8ecac986af048b5738c57.jpg");
            avatarUrls.add("/avt/z7342362678817_fdc3109318b02183ff70a23333c78bcf.jpg");
            avatarUrls.add("/avt/z7342362682020_3925a9b021be64cc5cbfa201fc75be00.jpg");

            return ResponseEntity.ok(ApiResponse.success("Default avatars retrieved successfully", avatarUrls));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Failed to load avatars"));
        }
    }
}
