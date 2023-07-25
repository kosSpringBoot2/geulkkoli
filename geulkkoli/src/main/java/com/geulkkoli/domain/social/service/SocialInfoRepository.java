package com.geulkkoli.domain.social.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialInfoRepository extends JpaRepository<SocialInfo, Long> {
    Optional<SocialInfo> findSocialInfoBySocialTypeAndSocialId(String socialType, String socialId);

    Optional<SocialInfo> findSocialInfoBySocialTypeAndAndUser_Email(String socialType, String email);

    Boolean existsBySocialTypeAndSocialId(String socialType, String socialId);

    List<SocialInfo> findAllByUser_Email(String email);

    Optional<SocialInfo> findSocialInfoBySocialId(String socialId);

    List<SocialInfo> findAllByUserNickName(String nickName);
}
