package com.jose.membershiprules;

import org.springframework.boot.SpringApplication;

public class TestMembershipRulesEngineApplication {

	public static void main(String[] args) {
		SpringApplication.from(MembershipRulesEngineApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
