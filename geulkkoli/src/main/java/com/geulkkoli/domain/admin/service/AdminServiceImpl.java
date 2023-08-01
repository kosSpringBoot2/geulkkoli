package com.geulkkoli.domain.admin.service;

import com.geulkkoli.domain.admin.AccountLock;
import com.geulkkoli.domain.hashtag.HashTag;
import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.post.PostRepository;
import com.geulkkoli.domain.posthashtag.PostHashTag;
import com.geulkkoli.domain.posthashtag.service.PostHashTagService;
import com.geulkkoli.domain.topic.Topic;
import com.geulkkoli.domain.topic.TopicRepository;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.UserRepository;
import com.geulkkoli.domain.admin.AccountLockRepository;
import com.geulkkoli.domain.admin.ReportRepository;
import com.geulkkoli.web.admin.DailyTopicDto;
import com.geulkkoli.web.admin.ReportDto;
import com.geulkkoli.web.post.dto.AddDTO;
import com.geulkkoli.web.post.dto.EditDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    private final PostRepository postRepository;

    private final ReportRepository reportRepository;

    private final AccountLockRepository accountLockRepository;

    private final PostHashTagService postHashTagService;

    private final TopicRepository topicRepository;

    public AccountLock lockUser(Long userId, String reason, Long lockDate) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
        LocalDateTime lockUntil = LocalDateTime.now().plusDays(lockDate);
        return accountLockRepository.save(user.lock(reason, lockUntil));
    }

    //신고받은 게시글 조회
    public List<ReportDto> findAllReportedPost() {
        List<Post> allPost = reportRepository.findDistinctByReportedPost();
        List<ReportDto> reportDtoList = new ArrayList<>();

        for (Post post : allPost) {
            reportDtoList.add(ReportDto.toDto(post, reportRepository.countByReportedPost(post)));
        }

        return reportDtoList;
    }

    //게시글 작성자 조회
    public User findUserByPostId(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        return post.getUser();
    }

    @Override
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found id matches:" + postId));
        Post deletePost = post.getUser().deletePost(post);
        ArrayList<PostHashTag> postHashTags = new ArrayList<>(post.getPostHashTags());
        for (PostHashTag postHashTag : postHashTags) {
            post.deletePostHashTag(postHashTag.getPostHashTagId());
        }
        postRepository.delete(deletePost);
    }

    public Post saveNotice(AddDTO post, User user) {
        Post writePost = user.writePost(post);
        Post save = postRepository.save(writePost);
        List<HashTag> hashTags = postHashTagService.hashTagSeparator("#공지글" + post.getTagListString());
        postHashTagService.addHashTagsToPost(save, hashTags);

        return save;
    }

    public void updateNotice(Long postId, EditDTO updateParam) {
        Post post = findById(postId)
                .getUser()
                .editPost(postId, updateParam);
        ArrayList<PostHashTag> postHashTags = new ArrayList<>(post.getPostHashTags());

        for (PostHashTag postHashTag : postHashTags) {
            post.deletePostHashTag(postHashTag.getPostHashTagId());
        }

        List<HashTag> hashTags = postHashTagService.hashTagSeparator("#공지글"
                + updateParam.getTagListString());
        postHashTagService.addHashTagsToPost(post, hashTags);

        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Post findById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found id matches:" + postId));
    }

    public List<DailyTopicDto> findWeeklyTopic() {
        List<Topic> topics = topicRepository.findTopicByUpComingDateBetween(LocalDate.now(), LocalDate.now().plusDays(29), Sort.by(Sort.Direction.ASC, "upComingDate"));

        if (topics.size() < 30) {
            topics = fillTopic(topics);
        }

        return topics.stream()
                .limit(10)
                .map(a -> DailyTopicDto.builder()
                        .topic(a.getTopicName())
                        .date(a.getUpComingDate().toString())
                        .build())
                .collect(Collectors.toList());
    }


    public List<Topic> fillTopic(List<Topic> topics) {
        for (int i = 0; i < 30; i++) {
            if (topics.size() == i || !topics.get(i).getUpComingDate().isEqual(LocalDate.now().plusDays(i))) {
                Topic topic = topicRepository.findTopicByUseDateBefore(LocalDate.now().minusDays(30));
                if (!topics.contains(topic)) {
                    topics.add(i, topic);
                    topics.get(i).settingUpComingDate(LocalDate.now().plusDays(i));
                } else {
                    i--;
                }
            }
        }
        topics.sort(Comparator.comparing(Topic::getUpComingDate));
        return topics;
    }

    public List<Topic> fillTopic2(List<Topic> topics) {

        topics.sort(Comparator.comparing(Topic::getUpComingDate));
        return topics;
    }

    public void updateTopic(DailyTopicDto topic) {
        Topic findTopic = topicRepository.findTopicByUpComingDate(LocalDate.parse(topic.getDate()));
        findTopic.settingUpComingDate(LocalDate.of(2000,1,1));

        Topic updateTopic = topicRepository.findTopicByTopicName(topic.getTopic());

        if(Objects.isNull(updateTopic)){
            updateTopic = Topic.builder().topicName(topic.getTopic()).build();
        }

        updateTopic.settingUpComingDate(LocalDate.parse(topic.getDate()));
        topicRepository.save(updateTopic);
    }
}
