package com.querydsl.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.dto.MemberSearchCondition;
import com.querydsl.dto.MemberTeamDto;
import com.querydsl.dto.QMemberTeamDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.querydsl.entity.QMember.member;
import static com.querydsl.entity.QTeam.team;

/**
 * 인터페이스인 MemberRepositoryCustom 구현
 * 해당 인터페이스를 구현한 사용자정의 클래스를 생성
 **/
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.name,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(nameEq(condition.getName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    private BooleanExpression nameEq(String name) {
        return StringUtils.hasText(name) ? member.name.eq(name) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer age) {
        return age == null ? null : member.age.goe(age);
    }

    private BooleanExpression ageLoe(Integer age) {
        return age == null ? null : member.age.loe(age);
    }
}
