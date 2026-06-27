package com.brace.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ServerApplicationTests {

	@Test
	@DisplayName("application context loads with testcontainers")
	void contextLoads() {
	}

}
