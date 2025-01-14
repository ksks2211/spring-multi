package org.example.proj;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author rival
 * @since 2025-01-15
 */

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;



    @GetMapping("")
    public List<PostDto> getPosts(){
        return postService.getPosts();
    }






    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable(name="id")Long id){
        postService.deletePost(id);
    }


    @GetMapping("/{id}")
    public PostDto getPost(@PathVariable(name="id") Long id){
        return postService.getPost(id);
    }



    @PostMapping("")
    public PostDto createPost(@RequestBody PostDto postDto){
        return postService.createPost(postDto.getTitle(), postDto.getContent());
    }




}
