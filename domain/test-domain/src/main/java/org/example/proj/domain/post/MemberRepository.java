package org.example.proj.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author rival
 * @since 2024-11-28
 */
public interface MemberRepository extends JpaRepository<Member,Long> {




    @Query("SELECT m FROM Member m where m.team.id =:teamId")
    List<Member> findMembersByTeamId(@Param("teamId") Long teamId);


    @Query("SELECT m FROM Member m where m.team.title =:title")
    List<Member> findMembersByTeamTitle(@Param("title") String title);




}
