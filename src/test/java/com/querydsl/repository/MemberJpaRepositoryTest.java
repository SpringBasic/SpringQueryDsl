package com.querydsl.repository;

import com.querydsl.dto.MemberSearchCondition;
import com.querydsl.dto.MemberTeamDto;
import com.querydsl.entity.Member;
import com.querydsl.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void basicQueryDslTest() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 20);
        Member member3 = new Member("member2", 20);

        memberJpaRepository.saveAll(Arrays.asList(member1, member2, member3));

        List<Member> findMember = this.memberJpaRepository.findAll();

        List<Member> member01 = this.memberJpaRepository.findAll_queryDsl();
    }

    @Test
    public void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);


        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition =
                MemberSearchCondition.builder()
                        .name("member1")
                        .teamName("teamA")
                        .ageGoe(20)
                        .ageLoe(41)
                        .build();

        // contains : 포함, 순서 고려 x
        // contains : 동일 + 순서 일치

        List<MemberTeamDto> result = this.memberJpaRepository.searchByBuilder(condition);
        for (MemberTeamDto memberTeamDto : result) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }
    }
}