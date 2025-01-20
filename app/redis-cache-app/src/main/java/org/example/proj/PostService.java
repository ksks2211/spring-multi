package org.example.proj;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proj.domain.post.Post;
import org.example.proj.domain.post.PostNotFoundException;
import org.example.proj.domain.post.PostRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author rival
 * @since 2025-01-15
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    public static final String CACHE_NAME="posts";

    private final PostRepository postRepository;

    private final CacheManager cacheManager;

    private Cache postsCache;

    @PostConstruct
    public void init(){
        this.postsCache = cacheManager.getCache(CACHE_NAME);
    }

    @CachePut(value=CACHE_NAME, key="#result.id")
    public PostDto createPost(String title, String content){
        Post post = Post.builder().title(title).content(content).build();
        Post savedPost = postRepository.save(post);
        PostDto postDto = fromEntityToDto(savedPost);

        log.info("PostDto : {}", postDto);


//        postsCache.put(postDto.getId(),postDto);
        return postDto;
    }



    @CacheEvict(value=CACHE_NAME, key="#id")
    public void deletePost(Long id){
        postRepository.deleteById(id);
    }


    @CachePut(value=CACHE_NAME, key="#result.id")
    public PostDto updatePost(Long id, String title, String content){
        Post post = postRepository.findById(id).orElseThrow(()-> new PostNotFoundException("Post Not Found"));
        post.setContent(content);
        post.setTitle(title);
        postRepository.save(post);
        return fromEntityToDto(post);
    }

    @Cacheable(value=CACHE_NAME, key = "#id")
    public PostDto getPost(Long id){
        Post post = postRepository.findById(id).orElseThrow(()->new PostNotFoundException("No Post"));
        return fromEntityToDto(post);
    }

    public List<PostDto> getPosts(){
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(this::fromEntityToDto).toList();
    }

    public PostDto fromEntityToDto(Post post){
        return PostDto.builder().title(post.getTitle()).id(post.getId()).content(post.getContent()).build();
    }
}
