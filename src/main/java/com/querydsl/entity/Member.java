package com.querydsl.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Slf4j
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@ToString(of = {"id","name","age"})
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @Builder
    public Member(String name, int age, Team team) {
        this.name = name;
        this.age = age;
        if(team != null) {
            this.changeTeam(team);
        }
    }

    private void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this); // 객체 지향
    }
}
