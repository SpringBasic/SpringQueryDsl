package com.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.dto.MemberSearchCondition;
import com.querydsl.dto.MemberTeamDto;
import com.querydsl.dto.QMemberTeamDto;
import com.querydsl.entity.Member;
import com.querydsl.entity.QTeam;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.querydsl.entity.QMember.member;
import static com.querydsl.entity.QTeam.team;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(@Autowired EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public void saveAll(List<Member> members) {
        for (Member member : members) {
            em.persist(member);
        }
    }

    public Optional<Member> findMemberById(Long id) {
        Member findMember = em.find(Member.class,id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m",Member.class)
                .getResultList();
    }

    public List<Member> findMemberByName(String name) {
        return em.createQuery("select m from Member m where m.name = :nane", Member.class)
                .getResultList();
    }


    public List<Member> findAll_queryDsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findMemberByName_queryDsl(String name) {
        return queryFactory
                .selectFrom(member)
                .where(member.name.eq(name))
                .from(member)
                .fetch();
    }

    /**
     * MemberSearchCondition 은 조건
    **/
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(StringUtils.hasText(condition.getName())) {
            booleanBuilder.and(member.name.eq(condition.getName()));
        }

        if(StringUtils.hasText(condition.getTeamName())) {
            booleanBuilder.and(team.name.eq(condition.getTeamName()));
        }

        if(condition.getAgeGoe() != null) {
            booleanBuilder.and(member.age.goe(condition.getAgeGoe()));
        }

        if(condition.getAgeLoe() != null) {
            booleanBuilder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.name,
                        member.age,
                        member.team.id.as("teamId"),
                        member.team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(booleanBuilder)
                .fetch();
    }
}
