package com.jose.membershiprules.member.infrastructure;

import com.jose.membershiprules.domain.MembershipType;
import com.jose.membershiprules.member.Member;
import com.jose.membershiprules.member.MemberDirectory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryMemberDirectory implements MemberDirectory {

    private final Map<String, Member> members = Map.ofEntries(
            Map.entry("BHFC-1001", new Member(
                    "BHFC-1001",
                    "111111",
                    MembershipType.SEASON_TICKET_HOLDER,
                    true,
                    LocalDate.of(2015, 6, 1)
            )),
            Map.entry("BHFC-12000", new Member(
                    "BHFC-12000",
                    "120000",
                    MembershipType.SEASON_TICKET_HOLDER,
                    true,
                    LocalDate.of(2014, 5, 10)
            )),
            Map.entry("BHFC-18050", new Member(
                    "BHFC-18050",
                    "180500",
                    MembershipType.SEASON_TICKET_HOLDER,
                    true,
                    LocalDate.of(2016, 9, 3)
            )),
            Map.entry("BHFC-27000", new Member(
                    "BHFC-27000",
                    "270000",
                    MembershipType.SEASON_TICKET_HOLDER,
                    true,
                    LocalDate.of(2017, 3, 22)
            )),
            Map.entry("BHFC-36035", new Member(
                    "BHFC-36035",
                    "360350",
                    MembershipType.SEASON_TICKET_HOLDER,
                    true,
                    LocalDate.of(2018, 8, 1)
            )),
            Map.entry("BHFC-42000", new Member(
                    "BHFC-42000",
                    "420000",
                    MembershipType.SEASON_TICKET_HOLDER,
                    true,
                    LocalDate.of(2021, 6, 15)
            )),
            Map.entry("BHFC-46000", new Member(
                    "BHFC-46000",
                    "460000",
                    MembershipType.SEASON_TICKET_HOLDER,
                    true,
                    LocalDate.of(2024, 9, 20)
            )),
            Map.entry("BHFC-1900", new Member(
                    "BHFC-1900",
                    "190000",
                    MembershipType.SEASON_TICKET_HOLDER,
                    false,
                    LocalDate.of(2013, 4, 12)
            )),
            Map.entry("BHFC-2045", new Member(
                    "BHFC-2045",
                    "482910",
                    MembershipType.PREMIUM_MEMBER,
                    true,
                    LocalDate.of(2021, 7, 14)
            )),
            Map.entry("BHFC-3107", new Member(
                    "BHFC-3107",
                    "739204",
                    MembershipType.STANDARD_MEMBER,
                    true,
                    LocalDate.of(2023, 2, 10)
            )),
            Map.entry("BHFC-4880", new Member(
                    "BHFC-4880",
                    "105377",
                    MembershipType.FREE_MEMBER,
                    true,
                    LocalDate.of(2024, 9, 3)
            )),
            Map.entry("BHFC-9001", new Member(
                    "BHFC-9001",
                    "900100",
                    MembershipType.PREMIUM_MEMBER,
                    false,
                    LocalDate.of(2020, 1, 20)
            ))
    );

    @Override
    public Optional<Member> findByMemberNumber(String memberNumber) {
        return Optional.ofNullable(members.get(memberNumber));
    }
}
