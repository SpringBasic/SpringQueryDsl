package com.querydsl;


import com.querydsl.entity.Member;
import com.querydsl.entity.QMember;
import com.querydsl.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryDsl;

    /**
     * -- 개별 테스트 실행 전에 실행 되는 메소드
     **/
    @BeforeEach
    public void before() {

        queryDsl = new JPAQueryFactory(em);

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
    }


    @Test
    @DisplayName("JPQL_TEST")
    void startJPQL() {
        Member member = em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", "member1")
                .getSingleResult();
        System.out.println(member.getName());

        assertThat(member.getTeam().getName()).isEqualTo("teamA");
    }

    @Test
    @DisplayName("QueryDsl_TEST")
    void startQueryDsl() {

        QMember qMember = member;

        Member foundMember = queryDsl.selectFrom(qMember)
                .where(qMember.name.eq("member1"))
                .fetchOne();

        assertThat(foundMember.getName()).isEqualTo("member1");
        assertThat(foundMember.getAge()).isEqualTo(10);
    }

    @Test
    @DisplayName("queryDsl_Search_TEST")
    void queryDslSearchTest() {

        // eq,ne,in,notin,between,goe,gt,loe,lt,like,contains,startswith

        Member member = queryDsl.selectFrom(QMember.member)
                .where(QMember.member.name.eq("member1")
                        .and(QMember.member.age.eq(10)))
                .fetchOne();

        assertThat(member).isNotNull();
        assertThat(member.getName()).isEqualTo("member1");
    }


    @Test
    @DisplayName("queryDsl_search_TEST02")
    void queryDslSearchTest02() {

        Member member = queryDsl.selectFrom(QMember.member)
                .where(QMember.member.name.contains("member1"),
                        QMember.member.age.eq(10))
                .fetchOne();
        assertThat(member).isNotNull();
        assertThat(member.getName()).contains("member1");
    }


    @Test
    @DisplayName("queryDsl_result_TEST")
    void queryDslResultTest(){

        List<Member> members = queryDsl.selectFrom(member)
                .fetch();

        // NonUniqueResultException
//        Member oneMember = queryDsl.selectFrom(member)
//                .fetchOne();

        Member firstMember = queryDsl.selectFrom(member)
                .fetchFirst();

        assertThat(members.size()).isEqualTo(4);
    }
}
