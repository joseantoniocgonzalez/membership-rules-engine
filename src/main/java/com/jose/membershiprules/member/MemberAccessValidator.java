package com.jose.membershiprules.member;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberAccessValidator {

    private final MemberDirectory memberDirectory;

    public MemberAccessValidator(MemberDirectory memberDirectory) {
        this.memberDirectory = memberDirectory;
    }

    public Optional<Member> validate(String memberNumber, String accessCode) {
        return memberDirectory.findByMemberNumber(memberNumber)
                .filter(member -> member.accessCode().equals(accessCode));
    }
}
