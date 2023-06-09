package com.geulkkoli.domain.follow.service;

import com.geulkkoli.domain.follow.Follow;
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


    public List<Follow> findAllFollowerByFolloweeId(Long followeeId, Pageable pageable) {
        return followRepository.findFollowEntitiesByFolloweeUserId(followeeId,pageable);
    }

    public List<Follow> findAllFolloweeByFollowerId(Long followerId, Pageable pageable) {
        return followRepository.findFollowEntitiesByFollowerUserId(followerId,pageable);
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