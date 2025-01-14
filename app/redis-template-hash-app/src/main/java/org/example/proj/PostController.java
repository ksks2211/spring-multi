package org.example.proj;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author rival
 * @since 2025-01-13
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;


    @PostMapping("")
    public Post createPost(@RequestBody PostCreateRequest post){
        return postService.createPost(post.getTitle(), post.getContent());
    }


    @GetMapping("")
    public Post getPost(){
        return postService.getPost();
    }

    @GetMapping("/count")
    public Post increase(){
        return postService.increaseCount();
    }


    @GetMapping("/title")
    public String getTitle(){
        return postService.getPostTitle();
    }




    @GetMapping("/delete")
    public String deletePost(){
        postService.deletePost();

        return "OK";
    }
}
