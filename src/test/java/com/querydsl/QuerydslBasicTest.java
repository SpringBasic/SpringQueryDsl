package com.querydsl;


import com.querydsl.core.Tuple;
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
    void queryDslSortTest(){

        /**
         * 1. 회원 나이 내림차순(desc)
         * 2. 회원 이름 오름차순(asc)
         * 단 2에서 회원 이름이 없으면 마지막 출력
        **/

        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> members = queryDsl
                .selectFrom(member)
                .where(member.age.eq(100)) // member 5, member 6
                .orderBy(member.age.desc(), member.name.asc().nullsLast())
                .fetch();

        for(Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.getName() = " + member.getName());
        }
        assertThat(members.get(0).getName()).isEqualTo("member5");
        assertThat(members.get(1).getName()).isEqualTo("member6");
        assertThat(members.get(2).getName()).isNull();
    }

    @Test
    @DisplayName("queryDsl_paging_TEST")
    void queryDslPagingTest(){

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
        for(Member m : members) {
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
        for(Member m : members02) {
            System.out.println("m.getName() = " + m.getName());
            System.out.println("m.getAge() = " + m.getAge());
        }
        System.out.println(members02.size());

        assertThat(members02.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("queryDsl_aggregation_TEST")
    void queryDslAggregationTest(){

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
    void queryDslGroupByTest(){

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
    void queryDslJoinTest(){

        // join(조인 대상, 별칭 으로 사용할 Q타입)
        List<Member> result = queryDsl
                .selectFrom(member)
                .join(member.team, team) // member 와 연관 관계 있는 team join
                .where(team.name.eq("teamA"))
                .fetch();

        for(Member member : result) {
            System.out.println(member);
        }

        // name 칼럼 데이터 가져오기
        assertThat(result).extracting("name").containsExactly("member1","member2");
    }

    @Test
    @DisplayName("queryDsl_theta_join_TEST")
    void queryDslThetaJoinTest(){

        // 연관 관계 없는 필드습 join = theta join
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryDsl
                .select(member)
                .from(member, team) // theta join
                .where(member.name.eq(team.name))
                .fetch();


        for(Member member : result) {
            System.out.println(member);
        }
    }

    @Test
    @DisplayName("queryDsl_join_on_TEST")
    void queryDslJoinOnTest(){

        // 회원과 팀 조회 하면서, 팀 이름이 teamA 인 팀만 조인, 회원은 모두 조회
        // jpql : select m, t from Member m left join m.team t on t.name = 'teamA';
        List<Tuple> result01 = queryDsl
                .select(member, team)
                .from(member)
                // left outer join -> teamA 가 아닌 member 여도 모두 조회
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for(Tuple tuple: result01) {
            System.out.println("tuple = " + tuple);
        }


        // 내부 조인인 경우, on == where (조인 대상 필터링)
        List<Tuple> result02 = queryDsl
                .select(member, team)
                .from(member)
                .innerJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        for(Tuple tuple : result02) {
            System.out.println("tuple = " + tuple);
        }
    }


    /**
     * - 연관관계 없는 필드로 조인 하는 실습
     * ex) 회원의 이름과 팀 이름이 같은 대상 외부 조인
    **/
    @Test
    @DisplayName("queryDsl_no_relation_join_on_TEST")
    void queryDslNoRelationJoinOnTest(){

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

        for(Tuple tuple : result) {
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
        
        for(Tuple tuple : result02) {
            System.out.println("tuple = " + tuple);
        }

        // 결과 : innerJoin 이기 때문에 member.name = team.name 인 경우 가져옴
    }


    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("queryDsl_fetch_join_TEST")
    void queryDslFetchJoinTest(){

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
    void queryDslSubQueryTest01(){

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
    void queryDslSubQueryTest02(){


        QMember subMember = new QMember("memberSub");

        List<Member> result02 = queryDsl
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(subMember.age.avg())
                                .from(subMember)
                ))
                .fetch();

        for(Member member : result02) {
            System.out.println("member = " + member);
        }
    }

    /**
     * 03. 서브 쿼리 in - 나이가 10살 보다 큰 나이 값에 나이가 속하는 모든 회원 조회
    **/
    @Test
    @DisplayName("queryDsl_subQuery_TEST03")
    void queryDslSubQueryTest03(){
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

        for(Member member : result03) {
            System.out.println("member = " + member);
        }
    }

    /**
     * 04. select 절에 서브 쿼리
    **/
    @Test
    @DisplayName("queryDsl_select_subQuery_TEST04")
    void queryDslSubQueryTest04(){

        QMember subMember = new QMember("subMember");

        List<Tuple> result = queryDsl
                .select(member.name,
                        // member 나이의 평균 값
                        JPAExpressions
                                .select(subMember.age.avg())
                                .from(subMember))
                .from(member)
                .fetch();

        for(Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }


        // JPA JPQL 서브 쿼리의 한계점 으로 from 절의 서브 쿼리는 지원 하지 않음
        // 따라서 JPQL 의 빌더 역할인 QueryDsl 또한 서브 쿼리 지원 x

        // from 절의 서브 쿼리 해결방안
        // - 서브 쿼리를 join 으로 변경
        // - 애플리케이션에서 쿼리를 2번 분리 해서 실행

    }
}
