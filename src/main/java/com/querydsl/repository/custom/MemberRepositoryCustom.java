package com.querydsl.repository.custom;

import com.querydsl.dto.MemberSearchCondition;
import com.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
    PageImpl<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    List<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
