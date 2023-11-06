package com.querydsl.repository;

import com.querydsl.entity.Member;
import com.querydsl.repository.custom.MemberRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    List<Member> findByName(String name);
}
