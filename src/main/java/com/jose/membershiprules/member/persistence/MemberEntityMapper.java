package com.jose.membershiprules.member.persistence;

import com.jose.membershiprules.member.Member;

public final class MemberEntityMapper {

    private MemberEntityMapper() {
    }

    public static Member toDomain(MemberEntity entity) {
        return new Member(
                entity.getMemberNumber(),
                entity.getAccessCode(),
                entity.getMembershipType(),
                entity.isActive(),
                entity.getMemberSince()
        );
    }
}
