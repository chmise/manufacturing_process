package com.u1mobis.dashboard_backend.security;

import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.entity.User;
import com.u1mobis.dashboard_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findWithCompanyByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        return new CustomUserPrincipal(user);
    }

    // 커스텀 UserDetails 구현
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();
            // 기본 사용자 권한 부여
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUserName();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        // User 엔티티에 직접 접근할 수 있는 메서드
        public User getUser() {
            return user;
        }

        public Long getUserId() {
            return user.getUserId();
        }

        public Long getCompanyId() {
            return user.getCompanyId();
        }

        // 로그인한 사용자의 회사 이름 (URL의 /api/{companyName}과 비교하는 값)
        public String getCompanyName() {
            Company company = user.getCompany();
            return company != null ? company.getCompanyName() : null;
        }

        // 로그인한 사용자의 회사 코드 (MQTT 토픽 factory/{companyCode}/...와 비교하는 값)
        public String getCompanyCode() {
            Company company = user.getCompany();
            return company != null ? company.getCompanyCode() : null;
        }
    }
}
