package com.querydsl.repository.custom;

import com.querydsl.dto.MemberSearchCondition;
import com.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
