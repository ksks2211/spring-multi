package org.example.proj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author rival
 * @since 2025-01-13
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final StringRedisTemplate redisTemplate;

    private final String POST_ID = "post-id";


    public Post createPost(String title, String content){
        Post post = Post.builder().id(POST_ID)
            .title(title)
            .content(content)
            .build();



        return postRepository.save(post);
    }


    public void deletePost(){
        postRepository.deleteById(POST_ID);
    }



    public Post getPost(){
        return postRepository.findById(POST_ID).orElseThrow(()->new RuntimeException("No Post"));
    }


    @Transactional
    public Post increaseCount(){

        redisTemplate.opsForHash().increment("Post:"+POST_ID, "count",1);

        return getPost();
    }

    public String getPostTitle(){

        String key = "Post:" + POST_ID;
        String field = "title";
        Boolean exists = redisTemplate.hasKey(key);

        log.info("Exists");

        if(exists){
            Object obj = redisTemplate.opsForHash().get(key, field);
            if(obj instanceof String title){
                return title;
            }
        }

        return "Not Found";

    }

}
