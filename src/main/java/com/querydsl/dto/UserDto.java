package com.querydsl.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"username","age"})
public class UserDto {
    private String username;
    private Integer age;
}
