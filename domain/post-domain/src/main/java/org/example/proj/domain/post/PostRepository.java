package org.example.proj.domain.post;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author rival
 * @since 2024-11-25
 */
public interface PostRepository extends JpaRepository<Post,Long>, JpaSpecificationExecutor<Post> {

    @Override
    @Query("SELECT p FROM Post p WHERE p.deleted = false")
    @NonNull List<Post> findAll();


    @Query("SELECT p FROM Post p WHERE p.deleted = true")
    @NonNull List<Post> findAllDeleted();



    @Query("SELECT p FROM Post p WHERE p.deleted = true and p.id = :id")
    Optional<Post> findDeletedById(@Param("id") Long id);



    @Override
    @Query("SELECT p FROM Post p WHERE p.deleted = false and p.id = :id")
    @NonNull Optional<Post> findById(@Param("id") @NonNull Long id);




    @Override
    @Modifying
    @Query("UPDATE Post p SET p.deleted = true, p.deletedAt = now() WHERE p.id = :id")
    void deleteById(@Param("id") @NonNull Long id);




}
