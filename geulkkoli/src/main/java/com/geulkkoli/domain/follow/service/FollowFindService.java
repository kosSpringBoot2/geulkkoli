package com.geulkkoli.domain.follow.service;

import com.geulkkoli.application.follow.FollowInfo;
import com.geulkkoli.domain.follow.FollowRepository;
import com.geulkkoli.domain.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FollowFindService {
    private final FollowRepository followRepository;

    public FollowFindService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }


    public List<FollowInfo> findAllFollowerByFolloweeId(Long followeeId, Long lastFollowId, Pageable pageable) {
        return followRepository.findFollowEntitiesByFolloweeUserId(followeeId, lastFollowId, pageable.getPageSize());
    }

    public List<FollowInfo> findAllFolloweeByFollowerId(Long followerId, Long lastFollowId, Pageable pageable) {
        return followRepository.findFollowEntitiesByFollowerUserId(followerId, lastFollowId, pageable.getPageSize());
    }

    public Integer countFollowerByFolloweeId(Long followeeId) {
        return followRepository.countByFolloweeUserId(followeeId);
    }

    public Integer countFolloweeByFollowerId(Long followerId) {
        return followRepository.countByFollowerUserId(followerId);
    }

    public Boolean checkFollow(User loggingUser, User authorUser) {
        return followRepository.existsByFolloweeUserIdAndFollowerUserId(authorUser.getUserId(), loggingUser.getUserId());
    }
}
