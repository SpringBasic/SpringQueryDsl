package com.querydsl.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSearchCondition {
    private String name;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
