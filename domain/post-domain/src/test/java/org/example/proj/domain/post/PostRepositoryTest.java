package org.example.proj.domain.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rival
 * @since 2024-11-25
 */


@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager testEntityManager;


    @BeforeEach
    void clear(){
        postRepository.deleteAll();
    }


    Post generatePost(String title, String content){
        return Post.builder().title(title).content(content).build();
    }


    Post createSamplePost(){
        String title = "title of post";
        String content = "content of post";

        Post post = generatePost(title, content);

        return postRepository.save(post);


    }

    // create
    @Test
    void createPost(){
        String title = "title of post";
        String content = "content of post";
        Post post = generatePost(title, content);
        Post savedPost = postRepository.save(post);
        Optional<Post> retrievedPost = postRepository.findById(savedPost.getId());
        assertTrue(retrievedPost.isPresent());
        assertEquals(title, retrievedPost.get().getTitle());
        System.out.println(savedPost);
    }

    @Test
    void createPostWithNotNullField(){
        String content = "content of post";
        Post post = generatePost(null, content);
        assertThrows(DataIntegrityViolationException.class, () -> postRepository.save(post));
    }

    // find all
    @Test
    void getPosts(){
        final int size = 5;
        List<Post> posts = IntStream.range(0, size).mapToObj(i -> {
            String title = String.format("title of post %d", i);
            String content = String.format("content of post %d", i);
            return generatePost(title, content);
        }).toList();
        postRepository.saveAllAndFlush(posts);
        List<Post> foundPosts = postRepository.findAll();
        assertEquals(size, foundPosts.size());
        System.out.println(posts);
    }



    @Test
    void softDeletePost(){

        // Given
        Post createdPost = createSamplePost();
        Long id = createdPost.getId();
        testEntityManager.flush();
        testEntityManager.clear();

        // When
        postRepository.deleteById(id);
        testEntityManager.flush();





        // Then
        Post deletedPost = postRepository.findDeletedById(id).orElseThrow();
        assertTrue(postRepository.findById(id).isEmpty());
        assertTrue(deletedPost.isDeleted());
        assertNotNull(deletedPost.getDeletedAt());
    }



    @Test
    void getNotDeletedPosts(){
        final int size = 5;
        List<Post> posts = IntStream.range(0, size).mapToObj(i -> {
            String title = String.format("title of post %d", i);
            String content = String.format("content of post %d", i);
            return generatePost(title, content);
        }).toList();
        postRepository.saveAll(posts);
        Long id = posts.get(0).getId();
        System.out.println(posts);
        testEntityManager.flush();
        testEntityManager.clear();


        postRepository.deleteById(id);
        testEntityManager.flush();




        Post deletedPost = postRepository.findDeletedById(id).orElseThrow();
        System.out.println(deletedPost);
        assertTrue(deletedPost.isDeleted());
        assertNotNull(deletedPost.getDeletedAt());

        List<Post> notDeletedPosts = postRepository.findAll(PostSpecifications.isNotDeleted());

        assertNotNull(notDeletedPosts);
        assertFalse(notDeletedPosts.isEmpty());
        assertEquals(size-1,notDeletedPosts.size() );
    }

    // find one
    @Test
    void getPost(){

    }
}