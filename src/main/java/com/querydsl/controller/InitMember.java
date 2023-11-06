package com.querydsl.controller;

import com.querydsl.entity.Member;
import com.querydsl.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/**
 * profile 이 local 일때 동작
**/
@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {
    private final InitMemberService initMemberService;


    /**
     * IOC 컨테이너 에서 빈을 생성할 때 해당 빈의 생성자를 호출
     * 생성자는 빈의 인스턴스를 초기화하는 역할을 함
     * 생성자 호출 후, 의존성 주입이 이루어진다.
    **/
    @PostConstruct
    public void init() {
        this.initMemberService.init();
    }

    /**
     * 스프링 빈 라이프 스타일 때문에 Transactional 과 PostConstruct 분리
    **/
    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100 ; i++) {
                Team selectedTeam = i % 2 == 0? teamA : teamB;
                em.persist(new Member("member" + i,i,selectedTeam));
            }
        }
    }
}
