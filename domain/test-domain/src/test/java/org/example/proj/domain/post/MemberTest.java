package org.example.proj.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rival
 * @since 2024-11-28
 */

@DataJpaTest
class MemberTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager testEntityManager;



    void prep_data(){
        Team teamA = Team.builder().title("teamA").build();
        Team teamB = Team.builder().title("teamB").build();

        teamRepository.saveAll(List.of(teamA,teamB));

        Member member1 = Member.builder().name("member1").team(teamA).build();
        Member member2 = Member.builder().name("member2").team(teamA).build();

        Member member3 = Member.builder().name("member3").team(teamB).build();
        Member member4 = Member.builder().name("member4").team(teamB).build();
        Member member5 = Member.builder().name("member5").team(teamB).build();


        memberRepository.saveAll(List.of(member1, member2,member3, member4, member5));


        testEntityManager.flush();
        testEntityManager.clear();

        System.out.println("=======================================");
    }
    @DisplayName("1. ")
    @Test
    void test_1() {
        prep_data();
        List<Member> members = memberRepository.findAll();

        for(Member member : members){
            System.out.println(member.getTeam());
        }

    }


    @DisplayName("2. ")
    @Test
    void test_2(){
        prep_data();

        List<Team> teams = teamRepository.findAll();



        for(Team team : teams){
            System.out.println(team.getMembers());
        }
    }


    @DisplayName("3. ")
    @Test
    void test_3(){

        prep_data();

        List<Team> teams = teamRepository.findAll();

        for(Team team : teams){
            List<Member> members = memberRepository.findMembersByTeamId(team.getId());
            System.out.println(members);
        }
    }

    @DisplayName("4. Join Fetch")
    @Test
    void test_4(){

        prep_data();


        List<Team> teams = teamRepository.findAllJoinFetch();

        for(Team team : teams){
            System.out.println(team.getMembers());
        }
    }


    @DisplayName("7. Join Fetch")
    @Test
    void test_7(){

        prep_data();


        List<Team> teams = teamRepository.findAllByEntityGraph();

        for(Team team : teams){
            System.out.println(team.getMembers());
        }
    }
    @DisplayName("1. ")
    @Test
    void test_6() {


    }

    @DisplayName("1. ")
    @Test
    void test_5() {

        prep_data();


        List<Member> members = memberRepository.findMembersByTeamTitle("teamA");

        System.out.println(members);
    }


    @Test
    void address_test(){


        Address address = Address.builder()
            .district("district")
            .city("ny")
            .zipCode("90210")
            .build();


        Set<String> roles = new HashSet<>(List.of("USER","ADMIN","MANAGER"));



        Member member = Member.builder()
            .address(address)
            .name("member1")
            .roles(roles)
            .build();

        memberRepository.save(member);


        memberRepository.flush();


        memberRepository.findAll().forEach(System.out::println);
    }



}