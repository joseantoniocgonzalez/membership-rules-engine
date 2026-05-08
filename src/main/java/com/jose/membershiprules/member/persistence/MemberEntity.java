package com.jose.membershiprules.member.persistence;

import com.jose.membershiprules.domain.MembershipType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "members")
public class MemberEntity {

    @Id
    @Column(name = "member_number", nullable = false, length = 20)
    private String memberNumber;

    @Column(name = "access_code", nullable = false, length = 20)
    private String accessCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false, length = 40)
    private MembershipType membershipType;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "member_since", nullable = false)
    private LocalDate memberSince;

    protected MemberEntity() {
    }

    public MemberEntity(
            String memberNumber,
            String accessCode,
            MembershipType membershipType,
            boolean active,
            LocalDate memberSince
    ) {
        this.memberNumber = memberNumber;
        this.accessCode = accessCode;
        this.membershipType = membershipType;
        this.active = active;
        this.memberSince = memberSince;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getMemberSince() {
        return memberSince;
    }
}
