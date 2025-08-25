package com.pharma.inventory.security;

import com.pharma.inventory.entity.User;
import com.pharma.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 사용자 정보 로드 서비스
 * Spring Security의 UserDetailsService 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("사용자 정보 로드 - 사용자명: {}", username);

        // 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음 - 사용자명: {}", username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });

        // 활성 상태 확인
        if (!user.getIsActive()) {
            log.error("비활성화된 계정 - 사용자명: {}", username);
            throw new UsernameNotFoundException("비활성화된 계정입니다: " + username);
        }

        return createUserDetails(user);
    }

    /**
     * User 엔티티를 UserDetails로 변환
     */
    private UserDetails createUserDetails(User user) {
        return CustomUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .department(user.getDepartment())
                .authorities(getAuthorities(user))
                .enabled(user.getIsActive())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    /**
     * 사용자 권한 생성
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 역할 기반 권한 추가
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // 역할별 추가 권한 (선택적)
        switch (user.getRole()) {
            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("MANAGE_USERS"));
                authorities.add(new SimpleGrantedAuthority("MANAGE_SYSTEM"));
                authorities.add(new SimpleGrantedAuthority("VIEW_REPORTS"));
                authorities.add(new SimpleGrantedAuthority("EXPORT_DATA"));
                break;
            case MANAGER:
                authorities.add(new SimpleGrantedAuthority("MANAGE_INVENTORY"));
                authorities.add(new SimpleGrantedAuthority("VIEW_REPORTS"));
                authorities.add(new SimpleGrantedAuthority("EXPORT_DATA"));
                break;
            case PHARMACIST:
                authorities.add(new SimpleGrantedAuthority("MANAGE_MEDICINE"));
                authorities.add(new SimpleGrantedAuthority("MANAGE_STOCK"));
                authorities.add(new SimpleGrantedAuthority("VIEW_REPORTS"));
                break;
            case DOCTOR:
                authorities.add(new SimpleGrantedAuthority("PRESCRIBE_MEDICINE"));
                authorities.add(new SimpleGrantedAuthority("VIEW_REPORTS"));
                break;
            case USER:
                authorities.add(new SimpleGrantedAuthority("VIEW_ONLY"));
                break;
        }

        return authorities;
    }
}