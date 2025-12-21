package course.security;

import course.model.enums.AccountStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final String id;
    private final String email;
    private final AccountStatus status;

    public CustomUserDetails(String id, String username, String password, String email, AccountStatus status,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, status == AccountStatus.ACTIVE, true, true, status == AccountStatus.ACTIVE,
                authorities);
        this.id = id;
        this.email = email;
        this.status = status;
    }
}
