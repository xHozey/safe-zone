package com.user.userService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"MONGODB_URI=mongodb://localhost:27017/userServiceTest",
		"JWT_SECRET=test-only-jwt-secret-with-at-least-thirty-two-characters"
})
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
