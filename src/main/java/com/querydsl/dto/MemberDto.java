package com.querydsl.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"name","age"})
public class MemberDto {
    private String name;
    private int age;
}
