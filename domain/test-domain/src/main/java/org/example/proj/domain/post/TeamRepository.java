package org.example.proj.domain.post;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author rival
 * @since 2024-11-28
 */
public interface TeamRepository extends JpaRepository<Team,Long> {



    @Query("SELECT t from Team t JOIN FETCH t.members")
    List<Team> findAllJoinFetch();





    @EntityGraph(attributePaths = {"members"})
    @Query("SELECT t FROM Team t")
    List<Team> findAllByEntityGraph();



}
