package com.querydsl;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.dto.MemberDto;
import com.querydsl.dto.QMemberDto;
import com.querydsl.dto.UserDto;
import com.querydsl.entity.Member;
import com.querydsl.entity.QMember;
import com.querydsl.entity.Team;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.querydsl.entity.QMember.member;
import static com.querydsl.entity.QTeam.team;
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
        Member member8 = new Member("member1", 80, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        Member member5 = new Member("member4", 40, teamB);
        Member member6 = new Member("member6", 30, teamA);
        Member member7 = new Member("member5",40,teamA);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);
        em.persist(member7);
        em.persist(member8);
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
    void queryDslResultTest() {

        // fetch,fetchOne,fetchFirst

        List<Member> members = queryDsl.selectFrom(member)
                .fetch();

        // NonUniqueResultException
//        Member oneMember = queryDsl.selectFrom(member)
//                .fetchOne();

        Member firstMember = queryDsl.selectFrom(member)
                .fetchFirst();

        assertThat(members.size()).isEqualTo(4);
    }

    @Test
    @DisplayName("queryDsl_sort_TEST")
    void queryDslSortTest() {

        /**
         * 1. 회원 나이 내림차순(desc)
         * 2. 회원 이름 오름차순(asc)
         * 단 2에서 회원 이름이 없으면 마지막 출력
         **/

        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> members = queryDsl
                .selectFrom(member)
                .where(member.age.eq(100)) // member 5, member 6
                .orderBy(member.age.desc(), member.name.asc().nullsLast())
                .fetch();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.getName() = " + member.getName());
        }
        assertThat(members.get(0).getName()).isEqualTo("member5");
        assertThat(members.get(1).getName()).isEqualTo("member6");
        assertThat(members.get(2).getName()).isNull();
    }

    @Test
    @DisplayName("queryDsl_paging_TEST")
    void queryDslPagingTest() {

        // member1 ~ member 4
        // offset : 시작 행(데이터), limit : offset 으로 부터 조회할 수


        /* 1. 전체 Member 조푀 */
        List<Member> allMembers = queryDsl
                .selectFrom(member)
                .orderBy(member.name.desc())
                .fetch();

        for (Member m : allMembers) {
            System.out.println(m.getName());
        }



        /* 2. 1번째 데이터 부터 2개 조회 */
        List<Member> members = queryDsl
                .selectFrom(member)
                .orderBy(member.name.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(members.size()).isEqualTo(2);
        System.out.println("offset = 1, limit = 2");
        for (Member m : members) {
            System.out.println("m.getName() = " + m.getName());
            System.out.println("m.getAge() = " + m.getAge());
        }
        System.out.println(members.size());

        // member3, member2
        assertThat(members.get(0).getName()).isEqualTo("member3");


        // 3. 3번째 데이터 부터 2개 조회 */
        List<Member> members02 = queryDsl
                .selectFrom(member)
                .orderBy(member.name.desc())
                .offset(3)
                .limit(2)
                .fetch();

        System.out.println("offset = 3, limit = 2");
        for (Member m : members02) {
            System.out.println("m.getName() = " + m.getName());
            System.out.println("m.getAge() = " + m.getAge());
        }
        System.out.println(members02.size());

        assertThat(members02.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("queryDsl_aggregation_TEST")
    void queryDslAggregationTest() {

        // queryDsl.select() 결과는 Member 가 아니라 여러 열이 합쳐진 대상
        // 실무 에서는 tuple 말고 dto 로 바로 변환해서 사용
        List<Tuple> result = queryDsl
                .select(
                        member.count(), // 4
                        member.age.sum(), // 100
                        member.age.avg(), // 25.0
                        member.age.max(), // 40
                        member.age.min() // 10
                ).from(member)
                .fetch();

        System.out.println(result.size());
        for (Tuple tuple : result) {
            System.out.println(tuple);
        }
    }

    @Test
    @DisplayName("queryDsl_groupBy_TEST")
    void queryDslGroupByTest() {

        List<Tuple> result02 = queryDsl
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        for (Tuple tuple : result02) {
            System.out.println(tuple);
        }
    }

    @Test
    @DisplayName("queryDsl_join_TEST")
    void queryDslJoinTest() {

        // join(조인 대상, 별칭 으로 사용할 Q타입)
        List<Member> result = queryDsl
                .selectFrom(member)
                .join(member.team, team) // member 와 연관 관계 있는 team join
                .where(team.name.eq("teamA"))
                .fetch();

        for (Member member : result) {
            System.out.println(member);
        }

        // name 칼럼 데이터 가져오기
        assertThat(result).extracting("name").containsExactly("member1", "member2");
    }

    @Test
    @DisplayName("queryDsl_theta_join_TEST")
    void queryDslThetaJoinTest() {

        // 연관 관계 없는 필드습 join = theta join
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryDsl
                .select(member)
                .from(member, team) // theta join
                .where(member.name.eq(team.name))
                .fetch();


        for (Member member : result) {
            System.out.println(member);
        }
    }

    @Test
    @DisplayName("queryDsl_join_on_TEST")
    void queryDslJoinOnTest() {

        // 회원과 팀 조회 하면서, 팀 이름이 teamA 인 팀만 조인, 회원은 모두 조회
        // jpql : select m, t from Member m left join m.team t on t.name = 'teamA';
        List<Tuple> result01 = queryDsl
                .select(member, team)
                .from(member)
                // left outer join -> teamA 가 아닌 member 여도 모두 조회
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result01) {
            System.out.println("tuple = " + tuple);
        }


        // 내부 조인인 경우, on == where (조인 대상 필터링)
        List<Tuple> result02 = queryDsl
                .select(member, team)
                .from(member)
                .innerJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result02) {
            System.out.println("tuple = " + tuple);
        }
    }


    /**
     * - 연관관계 없는 필드로 조인 하는 실습
     * ex) 회원의 이름과 팀 이름이 같은 대상 외부 조인
     **/
    @Test
    @DisplayName("queryDsl_no_relation_join_on_TEST")
    void queryDslNoRelationJoinOnTest() {

        // jpql :  select m,t from Member m left join Team t on m.name = t.name
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryDsl
                .select(member, team)
                .from(member)
                // leftJoin(member.team,team) 인 경우 member fk 와 team pk 로 매칭
                .leftJoin(team)
                .on(member.name.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }


        // 결과 : leftJoin 이기 때문에 모든 Member 엔티티 가져 오고,
        // member.name = team.name 인 경우 team 엔티티 가져 옴


        List<Tuple> result02 = queryDsl
                .select(member, team)
                .from(member)
                .innerJoin(team)
                .on(member.name.eq(team.name))
                .fetch();

        for (Tuple tuple : result02) {
            System.out.println("tuple = " + tuple);
        }

        // 결과 : innerJoin 이기 때문에 member.name = team.name 인 경우 가져옴
    }


    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("queryDsl_fetch_join_TEST")
    void queryDslFetchJoinTest() {

        em.flush();
        em.clear();

        Member result = queryDsl
                .selectFrom(member)
                .where(member.name.eq("member1"))
                .fetchOne();

        System.out.println("result = " + result);

        // 해당 엔티티가 영속성 컨텍스트에 로딩이 된 엔티티인지 확인
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(result.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();


        em.flush();
        em.clear();

        Member result02 = queryDsl
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.name.eq("member1"))
                .fetchOne();


        boolean loaded02 = emf.getPersistenceUnitUtil().isLoaded(result02.getTeam());
        assertThat(loaded02).as("페지 조인 적용").isTrue();
    }


    /**
     * 01. 나이가 가장 많은 회원 조회
     **/
    @Test
    @DisplayName("queryDsl_subQuery_TEST01")
    void queryDslSubQueryTest01() {

        // subQuery = JPAExpressions

        QMember subMember = new QMember("memberSub");

        List<Member> result01 = queryDsl
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(subMember.age.max())
                                .from(subMember)
                ))
                .fetch();

        assertThat(result01).extracting("age")
                .containsExactly(40);
    }

    /**
     * 02. 나이가 평균 이상인 회원 조회
     **/

    @Test
    @DisplayName("queryDsl_subQuery_TEST02")
    void queryDslSubQueryTest02() {


        QMember subMember = new QMember("memberSub");

        List<Member> result02 = queryDsl
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(subMember.age.avg())
                                .from(subMember)
                ))
                .fetch();

        for (Member member : result02) {
            System.out.println("member = " + member);
        }
    }

    /**
     * 03. 서브 쿼리 in - 나이가 10살 보다 큰 나이 값에 나이가 속하는 모든 회원 조회
     **/
    @Test
    @DisplayName("queryDsl_subQuery_TEST03")
    void queryDslSubQueryTest03() {
        QMember subMember = new QMember("memberSub");

        List<Member> result03 = queryDsl
                .selectFrom(member)
                .where(member.age.in(
                        // Member Age 중 10 보다 큰 age 값 서브쿼리
                        JPAExpressions
                                .select(subMember.age)
                                .from(subMember)
                                .where(subMember.age.gt(10))
                ))
                .fetch();

        for (Member member : result03) {
            System.out.println("member = " + member);
        }
    }

    /**
     * 04. select 절에 서브 쿼리
     **/
    @Test
    @DisplayName("queryDsl_select_subQuery_TEST04")
    void queryDslSubQueryTest04() {

        QMember subMember = new QMember("subMember");

        List<Tuple> result = queryDsl
                .select(member.name,
                        // member 나이의 평균 값
                        JPAExpressions
                                .select(subMember.age.avg())
                                .from(subMember))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }


        // JPA JPQL 서브 쿼리의 한계점 으로 from 절의 서브 쿼리는 지원 하지 않음
        // 따라서 JPQL 의 빌더 역할인 QueryDsl 또한 서브 쿼리 지원 x

        // from 절의 서브 쿼리 해결방안
        // - 서브 쿼리를 join 으로 변경
        // - 애플리케이션에서 쿼리를 2번 분리 해서 실행

    }

    @Test
    @DisplayName("queryDsl_case_TEST01")
    void queryDslCaseTest01() {
        List<String> result = queryDsl
                .select(member.age
                        .when(10).then("10살")
                        .when(20).then("20살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * - 복잡한 조건인 경우
     **/
    @Test
    @DisplayName("queryDsl_case_TEST02")
    void queryDslCaseTest02() {

        List<String> result = queryDsl
                .select(new CaseBuilder()
                        .when(member.age.between(10, 20)).then("0 ~ 20 살")
                        .when(member.age.between(21, 30)).then("21살 ~ 30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (int i = 0; i < result.size(); i++) {
            System.out.println("result.get(i) = " + result.get(i));
        }

        // DB 는 그냥 최소한의 필터링 그룹화
        // case 는 application / presentation layer 에서 이런 로직 처리하는 것이 이상
    }


    /**
     * - 0 ~ 30 살이 아닌 회원을 가장 먼저 출력
     * - 0 ~ 20 살 회원을 2번째로 출력
     * - 21 ~ 30 살 회원을 3번째로 출력
     */
    @Test
    @DisplayName("queryDsl_case_TEST03")
    void queryDslCaseTest03() {

        // 추후 숫자가 높은 것부터 출력 예정
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(21, 30)).then(1)
                .when(member.age.between(0, 20)).then(2)
                .otherwise(3);

        List<Tuple> result = queryDsl
                // member 이름, member 나이, 순서
                .select(member.name, member.age, rankPath)
                .from(member)
                // 순서가 높은 것 부터
                .orderBy(rankPath.desc())
                .fetch();

        for (Tuple tuple : result) {
            String name = tuple.get(member.name);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);

            System.out.println("name = " + name + " age = " + age + " rank = " + rank);
        }
    }

    /**
     * - 상수 실습 : 회원 조회 시 열 'A' 추가
     **/
    @Test
    @DisplayName("queryDsl_constant_TEST01")
    void queryDslConstantTest01() {
        List<Tuple> result = queryDsl
                .select(member.name, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * - 문자 더하기 실습(조회 데이터에 문자 더하기)
     **/
    @Test
    @DisplayName("queryDsl_concat_TEST")
    void queryDslConcatTest() {

        List<String> result = queryDsl
                // age 는 Int -> StringValue() 사용
                .select(member.name.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.age.eq(10))
                .fetch();

        System.out.println("result = " + result);

        assertThat(result).isNotNull();
        assertThat(result.get(0)).isEqualTo("member1_10");
    }

    @Test
    void queryDslOrderByTest() {
        List<Member> result = queryDsl
                .selectFrom(member)
                .orderBy(member.age.desc(), member.name.asc().nullsLast())
                .fetch();

        for(Member member : result) {
            System.out.println("member = " + member);
        }

        // member.name.asc().nullsLast() 의 의미 :
        // 나이 가 동일할 때, 이름으로 정렬 기준을 잡는데, 이때 null 이면 해당 나이 기준 순서 범위에서 마지막
        // ex)
        // member(id = 4, name = member4, age = 40)
        // member(id = 7, name = member5, age = 40)
        // member(id = 5, name = null, age = 40)
        // member(id = 6, name = member3, age = 30)
        // ...
    }


    // 프로젝션 : sql 에서 select 대상

    @Test
    @DisplayName("queryDsl_projection_test")
    void queryDslProjectionTest(){
        // select 절의 타입이 단건이 아니면 Tuple OR Dto 사용
        List<Tuple> result = queryDsl
                .select(member.name, member.age)
                .from(member)
                .fetch();

        for(Tuple tuple : result) {
            // tuple.get(열의 이름)
            String name = tuple.get(member.name);
            Integer age = tuple.get(member.age);
            System.out.println("name = " + name);
            System.out.println("age = " + age);
        }
    }

    @Test
    public void findDtoByJPQL() {
        // 순수 JPA 에서 jpql 을 사용해서 dto 로 받기 위해서는 dto 생성자 호출하는 것 처럼 사용
        Member result = em.createQuery("select new com.querydsl.dto.MemberDto(m.name, m.age) from Member m", Member.class)
                .getSingleResult();
    }

    /**
     * queryDsl 에서는 결과를 DTO 로 반환할 때 3가지 방법 지원
     * - 프로퍼티 접근(setter)
     * - 필드 직접 접근
     * - 생성자 사용
    **/
    @Test
    public void findDtoByQueryDslSetter() {
        // 1. 프로퍼티 접근을 이용한 프로젝션
        List<MemberDto> result = queryDsl
                // setter 을 사용하기 위해서 Projections 사용
                .select(Projections.bean(MemberDto.class,
                        member.name,
                        member.age))
                .from(member)
                .fetch();

        for(MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }
    
    
    @Test
    public void findDtoByQueryDslField() {
        // 2. 필드 접근을 이용한 프로젝션
        List<MemberDto> result = queryDsl
                .select(Projections.fields(MemberDto.class,
                        member.name,
                        member.age))
                .from(member)
                .fetch();
        
        for(MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByQueryDslConstructor() {
        // 3. 생성자를 이용한 프로젝션
        List<MemberDto> result = queryDsl
                .select(Projections.constructor(MemberDto.class,
                        member.name,
                        member.age))
                .from(member)
                .fetch();

        for(MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByQueryDslUsingDifferentName() {
        // 4. 엔티티의 필드명과 응답 DTO 필드명이 다른 경우
        List<UserDto> result = queryDsl
                .select(Projections.fields(UserDto.class,
                        member.name.as("username"),
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findDtoByQueryDslUsingDifferentNameUsingExpressionUtils() {
        // 5. ExpressionUtil + Dto Projections
        QMember subMember = new QMember("subMember");

        List<Tuple> result = queryDsl
                .select(Projections.fields(UserDto.class),
                        member.name.as("username"),
                        // ExpressionUtils -> 서브 쿼리 결과 별칭 적용
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(subMember.age.max())
                                        .from(subMember), "age"
                        ))
                .from(member)
                .fetch();


        for (Tuple tuple : result) {
            System.out.println("tuple.get(member.name) = " + tuple.get(member.name));
            System.out.println("tuple.get(member.age) = " + tuple.get(member.age));
        }
    }

    // @QueryProjection 사용
    @Test
    public void findDyoByQueryDslUsingQueryProjection() {
        List<MemberDto> result = queryDsl
                .select(new QMemberDto(member.name, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
        // Projections.constructor 와 보다 좋음 -> 컴파일 오류를 잡아낼 수 있다.

        // 단점 :
        // 1. Q 파일을 추가적으로 생성해야 한다.
        // 2. Dto 가 QueryDsl 에 대한 의존성이 생긴다.(순수 x)
    }

    @Test
    public void dynamicQuery_BooleanBuilder() {
        String nameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(nameParam,ageParam);
        // 파라미터가 null 인지 아닌지 따라 결과물이 달라짐
        // nameParam = "member1", ageParam = null & nameParam = null, ageParam = 10
        assertThat(result.size()).isEqualTo(2);
    }

    private List<Member> searchMember1(String nameParam, Integer ageParam) {

        BooleanBuilder builder = new BooleanBuilder();
        // BooleanBuilder 을 통해 동적 쿼리 작성
        if(nameParam != null) {
            builder.and(member.name.eq(nameParam));
        }

        if(ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        return queryDsl
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void dynamicQuery_WhereParam() {
        String nameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember2(nameParam,ageParam);
    }

    private List<Member> searchMember2(String nameParam, Integer ageParam) {
        return queryDsl
                .selectFrom(member)
                // where 절이 null 일때 무시
                .where(usernameEq(nameParam), ageEq(ageParam))
                .fetch();
    }

    private BooleanExpression usernameEq(String nameParam) {
        return nameParam == null ? null : member.name.eq(nameParam);
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam == null ? null : member.age.eq(ageParam);
    }


    // 벌크 연산 -> 배치 쿼리
    @Test
    public void bulkCalculation01() {
        // 일반적인 변경 감지는 하나씩 수정 쿼리가 생성 -> 벌크 연산 별도 추가 해야 함
        long result = queryDsl
                .update(member)
                .set(member.name, "비회원")
                .where(member.age.lt(28))
                .execute();

        // 벌크 연산을 하면 영속성 컨텍스트 초기화 필수
        em.flush();
        em.clear();

        /**
         * 벌크 연산의 주의점
         * - 1. 영속성 컨텍스트를 거치지 않고 바로 DB sql 쿼리를 날린다.
         * - 2. 영속성 컨텍스트와 DB 상태가 달라짐
         * - 3. 영속성 컨텍스트가 우선
        **/


        // 영속성 컨텍스트에서 데이터를 가져 온다
        List<Member> members = queryDsl
                .selectFrom(member)
                .fetch();

        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void bulkCalculation02() {
        // 모든 회원 나이 + 1
        queryDsl
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();

        // 50 살 이상 회원 삭제
        queryDsl
                .delete(member)
                .where(member.age.gt(50))
                .execute();
    }
}
