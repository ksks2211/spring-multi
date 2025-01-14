package org.example.proj.domain.post;

import org.springframework.data.jpa.domain.Specification;

/**
 * @author rival
 * @since 2024-11-27
 */
public class PostSpecifications {


    public static Specification<Post> isNotDeleted(){
        return (root, query, criteriaBuilder)-> criteriaBuilder.isFalse(root.get(Post_.DELETED));
    }

    public static Specification<Post> isDeleted(){
        return Specification.not(isNotDeleted());
    }



}
