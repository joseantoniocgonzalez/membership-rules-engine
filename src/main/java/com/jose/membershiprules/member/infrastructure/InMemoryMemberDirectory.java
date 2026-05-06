package com.jose.membershiprules.member.infrastructure;

import com.jose.membershiprules.domain.MembershipType;
import com.jose.membershiprules.member.Member;
import com.jose.membershiprules.member.MemberDirectory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryMemberDirectory implements MemberDirectory {

    private final Map<String, Member> members = Map.of(
            "BHFC-1001", new Member("BHFC-1001", "111111", MembershipType.SEASON_TICKET_HOLDER, true),
            "BHFC-2045", new Member("BHFC-2045", "482910", MembershipType.PREMIUM_MEMBER, true),
            "BHFC-3107", new Member("BHFC-3107", "739204", MembershipType.STANDARD_MEMBER, true),
            "BHFC-4880", new Member("BHFC-4880", "105377", MembershipType.FREE_MEMBER, true),
            "BHFC-9001", new Member("BHFC-9001", "900100", MembershipType.PREMIUM_MEMBER, false)
    );

    @Override
    public Optional<Member> findByMemberNumber(String memberNumber) {
        return Optional.ofNullable(members.get(memberNumber));
    }
}
