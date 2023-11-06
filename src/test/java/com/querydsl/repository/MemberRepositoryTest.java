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
public class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basicTest() {
        Member member1 = new Member("member1",10);
        Member member2 = new Member("member2",20);

        memberRepository.save(member1); memberRepository.save(member2);

        Member member = memberRepository.findById(member1.getId()).get();
        assertThat(member).isEqualTo(member1);

        List<Member> result = memberRepository.findAll();
        assertThat(result).containsExactly(member1,member2);
    }

    @Test
    public void queryDsl_SpringDataJPATest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamA);
        Member member3 = new Member("member3", 10, teamA);
        Member member4 = new Member("member4", 10, teamA);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition cond = new MemberSearchCondition();
        cond.setName("member1");
        cond.setAgeGoe(10);
        cond.setAgeLoe(40);
        cond.setTeamName("teamA");

        List<MemberTeamDto> result = this.memberRepository.search(cond);

        for (MemberTeamDto memberTeamDto : result) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }

        /**
         * 특정 기능에 특화된 것이라면 별도의 레포지토리 클래스를 만들자!!!!
        **/
    }
}
