package com.api.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "JWT_SECRET=test-only-jwt-secret-with-at-least-thirty-two-characters")
class GatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
