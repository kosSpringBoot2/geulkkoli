package com.geulkkoli.domain.user;

import com.geulkkoli.domain.user.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUserId(String loginId);
}
