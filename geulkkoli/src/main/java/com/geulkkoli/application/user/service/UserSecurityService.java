package com.geulkkoli.application.user.service;

import com.geulkkoli.application.security.AccountStatus;
import com.geulkkoli.application.security.Role;
import com.geulkkoli.application.security.RoleException;
import com.geulkkoli.application.user.CustomAuthenticationPrinciple;
import com.geulkkoli.application.user.UserModelDto;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.UserRepository;
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
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("loadUserByUsername : {}", email);
        Optional<User> findByEmailUser = userRepository.findByEmail(email);
        if (findByEmailUser.isEmpty()) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }

        User user = findByEmailUser.get();
        List<GrantedAuthority> authorities = new ArrayList<>();
        List<GrantedAuthority> grantedAuthorities = authorizeRole(authorities, user);

        if (authorities.isEmpty()) {
            throw new RoleException("권한을 찾을 수 없습니다.");
        }

        UserModelDto userModel = UserModelDto.toDto(user);

        if (TRUE.equals(user.isLock())) {
            return CustomAuthenticationPrinciple.from(userModel, authorities, AccountStatus.LOCKED);
        }
        return CustomAuthenticationPrinciple.from(userModel, grantedAuthorities, AccountStatus.ACTIVE);
    }


    private List<GrantedAuthority> authorizeRole(List<GrantedAuthority> authorities, User user) {
        if (TRUE.equals(user.getRole().isAdmin())) {
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getRoleName()));
            return authorities;
        }
        authorities.add(new SimpleGrantedAuthority(Role.USER.getRoleName()));
        return authorities;

    }

}