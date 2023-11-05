package com.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"name","age"})
public class MemberDto {
    private String name;
    private int age;

    @QueryProjection
    public MemberDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
