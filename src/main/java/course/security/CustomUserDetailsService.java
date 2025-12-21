package course.security;

import course.model.Admin;
import course.model.User;
import course.model.enums.AccountStatus;
import course.repository.AdminRepository;
import course.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username).orElse(null);
        if (admin != null) {
            return buildUserDetails(admin.getId(), admin.getUsername(), admin.getPassword(), admin.getEmail(),
                    "ROLE_ADMIN", admin.getStatus());
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return buildUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(),
                "ROLE_USER", user.getStatus());
    }

    public UserDetails loadUserByUsernameAndRole(String username, String role) throws UsernameNotFoundException {
        if ("ROLE_ADMIN".equals(role)) {
            Admin admin = adminRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + username));
            return buildUserDetails(admin.getId(), admin.getUsername(), admin.getPassword(), admin.getEmail(),
                    "ROLE_ADMIN", admin.getStatus());
        } else if ("ROLE_USER".equals(role)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return buildUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(),
                    "ROLE_USER", user.getStatus());
        } else {
            return loadUserByUsername(username);
        }
    }

    private UserDetails buildUserDetails(String id, String username, String password, String email,
            String role, AccountStatus status) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));

        return new CustomUserDetails(
                id,
                username,
                password,
                email,
                status,
                authorities);
    }
}
