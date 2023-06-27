package com.geulkkoli.domain.posthashtag;

import com.geulkkoli.domain.hashtag.HashTag;
import com.geulkkoli.domain.hashtag.HashTagRepository;
import com.geulkkoli.domain.hashtag.HashTagType;
import com.geulkkoli.domain.post.AdminTagAccessDenied;
import com.geulkkoli.domain.post.Post;
import com.geulkkoli.web.post.dto.PostRequestListDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostHashTagService {

    private final HashTagRepository hashTagRepository;
    private final PostHashTagRepository postHashTagRepository;

    //게시글에 해시태그 1개를 추가합니다
    public Long addHashTagToPost(Post post, HashTag tag) {
        return postHashTagRepository.save(post.addHashTag(tag)).getPostHashTagId();
    }


    //게시글에 다수의 해시태그를 추가합니다
    public void addHashTagsToPost(Post post, List<HashTag> tags) {
        for (HashTag tag : tags) {
            addHashTagToPost(post, tag);
        }
    }

    //게시판을 들어갔을 때, 게시글을 검색할 때 등 게시글을 가져오는 모든 경우에 쓰입니다.
    public Page<PostRequestListDTO> searchPostsListByHashTag(Pageable pageable, String searchType, String searchWords) {

        String searchWord = searchWordExtractor(searchWords);
        List<HashTag> tags = hashTagSeparator(searchWords);

        List<Post> posts = searchPostContainAllHashTags(tags);

        List<Post> resultList;
        switch (searchType) {
            case "제목":
                resultList = posts.stream()
                        .filter(post -> post.getTitle().contains(searchWord))
                        .collect(Collectors.toList());
                break;
            case "본문":
                resultList = posts.stream()
                        .filter(post -> post.getPostBody().contains(searchWord))
                        .collect(Collectors.toList());
                break;
            case "닉네임":
                resultList = posts.stream()
                        .filter(post -> post.getNickName().contains(searchWord))
                        .collect(Collectors.toList());
                break;
            case "해시태그":
                resultList = posts.stream()
                        .filter(post -> post.getPostHashTags().stream()
                                .anyMatch(postHashTag -> postHashTag.getHashTag().getHashTagName().contains(searchWord)))
                        .collect(Collectors.toList());
                break;
            default:
                resultList = new ArrayList<>(posts);
                break;
        }

        return getPosts(pageable, resultList).map(post -> new PostRequestListDTO(
                post.getPostId(),
                post.getTitle(),
                post.getNickName(),
                String.valueOf(post.getUpdatedAt()),
                post.getPostHits()
        ));
    }

    //searchPostsListByHashTag의 페이징 처리를 위해, 페이징 값을 반환해줍니다.
    private Page<Post> getPosts(Pageable pageable, List<Post> resultList) {
        resultList.sort(Comparator.comparing(Post::getUpdatedAt).reversed());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), resultList.size());
        return new PageImpl<>(resultList.subList(start, end), pageable, resultList.size());
    }

    //웹에서 받은 문자열로 하여금 해시태그로 나눠줍니다.
    public List<HashTag> hashTagSeparator(String searchWords) {
        Set<HashTag> hashTags = new LinkedHashSet<>();
        String[] splitter = searchWords.split("#");
        log.info("splitter: {}", Arrays.stream(splitter).collect(Collectors.toUnmodifiableList()));
        for (int i = 1; i < splitter.length; i++) {
            String stripper = splitter[i].strip();
            String trim = stripper.trim();
            log.info("trim: {}", trim);
            HashTag hashTagByHashTagName = hashTagRepository.findByHashTagName(trim);
            log.info("hashTagByHashTagName: {}", hashTagByHashTagName);
            if (hashTagByHashTagName != null) {
                hashTags.add(hashTagByHashTagName);
            } else {
                HashTag save = hashTagRepository.save(new HashTag(trim, HashTagType.GENERAL));
                hashTags.add(save);
            }
        }

        return new ArrayList<>(hashTags);
    }

    //웹에서 받은 문자열중 검색어를 추출해냅니다.
    public String searchWordExtractor(String searchWords) {
        String[] splitter = searchWords.split("#");
        return splitter[0].strip();
    }

    //해당 태그를 가진 게시글을 찾아냅니다.
    public List<Post> searchPostContainAllHashTags(List<HashTag> tags) {
        HashTag tag = tags.isEmpty() ? hashTagRepository.findByHashTagName("일반") : tags.get(0);

        List<Post> posts = new ArrayList<>();

        List<PostHashTag> postHashTagList = postHashTagRepository.findAllByHashTag(tag);

        for (PostHashTag postHashTag : postHashTagList) {
            Set<HashTag> postHashTags = postHashTag.
                    getPost().
                    getPostHashTags().
                    stream().
                    map(PostHashTag::getHashTag).
                    collect(java.util.stream.Collectors.toSet());

            if (postHashTags.containsAll(tags)) {
                posts.add(postHashTag.getPost());
            }
        }

        return posts;
    }

    //웹에서 분류나 상태값을 받아오지 못하거나 관리 태그에 접근하려 하는 경우, 이를 막습니다.
    public void validatePostHasType(List<HashTag> tags) {
        if (tags.stream().noneMatch(a -> a.getHashTagType().equals(HashTagType.CATEGORY))) {
            throw new IllegalArgumentException("분류");
        }
        if (tags.stream().noneMatch(a -> a.getHashTagType().equals(HashTagType.STATUS))) {
            throw new IllegalArgumentException("상태");
        }

        String managementTag = tags.stream().filter(a -> a.getHashTagType().equals(HashTagType.MANAGEMENT)).findAny().orElseGet(HashTag::new).getHashTagName();

        if (tags.stream().anyMatch(a -> a.getHashTagType().equals(HashTagType.MANAGEMENT))) {
            throw new AdminTagAccessDenied(managementTag);
        }

    }


}