package com.geulkkoli.domain.hashtag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HashTagsRepository extends JpaRepository<HashTags, Long> {

}