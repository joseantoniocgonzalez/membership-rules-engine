package com.jose.membershiprules.member;

import java.util.Optional;

public interface MemberDirectory {

    Optional<Member> findByMemberNumber(String memberNumber);
}
