package com.jose.membershiprules.member.persistence;

import com.jose.membershiprules.member.Member;
import com.jose.membershiprules.member.MemberDirectory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaMemberDirectory implements MemberDirectory {

    private final MemberJpaRepository memberJpaRepository;

    public JpaMemberDirectory(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Optional<Member> findByMemberNumber(String memberNumber) {
        return memberJpaRepository.findById(memberNumber)
                .map(MemberEntityMapper::toDomain);
    }
}
