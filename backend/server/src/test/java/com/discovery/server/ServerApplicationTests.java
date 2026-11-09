package com.discovery.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class ServerApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainDelegatesToSpringApplication() {
		String[] args = { "--spring.main.web-application-type=none" };
		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			ServerApplication.main(args);
			springApplication.verify(() -> SpringApplication.run(ServerApplication.class, args));
		}
	}

}
