package com.jose.membershiprules;

import com.jose.membershiprules.domain.MembershipType;
import com.jose.membershiprules.member.Member;
import com.jose.membershiprules.member.MemberDirectory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MemberDirectoryIntegrationTest {

    @Autowired
    private MemberDirectory memberDirectory;

    @Test
    void shouldReadPremiumMemberFromPostgreSql() {
        Optional<Member> member = memberDirectory.findByMemberNumber("BHFC-2045");

        assertThat(member).isPresent();
        assertThat(member.get().memberNumber()).isEqualTo("BHFC-2045");
        assertThat(member.get().accessCode()).isEqualTo("482910");
        assertThat(member.get().membershipType()).isEqualTo(MembershipType.PREMIUM_MEMBER);
        assertThat(member.get().active()).isTrue();
        assertThat(member.get().memberSince()).isEqualTo("2021-07-14");
    }

    @Test
    void shouldReadSeasonTicketHolderWithSeniorityFromPostgreSql() {
        Optional<Member> member = memberDirectory.findByMemberNumber("BHFC-36035");

        assertThat(member).isPresent();
        assertThat(member.get().membershipType()).isEqualTo(MembershipType.SEASON_TICKET_HOLDER);
        assertThat(member.get().active()).isTrue();
        assertThat(member.get().memberSince()).isEqualTo("2018-08-01");
    }

    @Test
    void shouldReturnEmptyWhenMemberDoesNotExist() {
        Optional<Member> member = memberDirectory.findByMemberNumber("BHFC-99999");

        assertThat(member).isEmpty();
    }
}
