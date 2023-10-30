package com.querydsl;

import com.querydsl.entity.Hello;
import com.querydsl.entity.QHello;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

    @Autowired
    EntityManager em;
    @Test
    void contextLoads() {
        Hello hello = new Hello();
        em.persist(hello);

        // 1. JPAQueryFactory 생성
        JPAQueryFactory query = new JPAQueryFactory(em);
        // 2. Q 객체 생성
        QHello qhello = QHello.hello;

        // 3. querydsl 실행
        Hello result = query
                .selectFrom(qhello)
                .fetchOne();

        assertThat(result).isEqualTo(hello);
        assertThat(result.getId()).isEqualTo(hello.getId());
    }
}
