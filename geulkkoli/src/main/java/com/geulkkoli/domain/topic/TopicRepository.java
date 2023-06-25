package com.geulkkoli.domain.topic;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    @Query(value = "SELECT * FROM Topic t order by RAND() limit 30",nativeQuery = true)
    List<Topic> findTopicByUseDateBefore(LocalDate time);

    List<Topic> findTopicByUpComingDateAfter(LocalDate time, Sort sort);

    List<Topic> findTopicByUpComingDate(LocalDate time);
}
