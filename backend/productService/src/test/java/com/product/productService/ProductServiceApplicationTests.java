package com.product.productService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "MONGODB_URI=mongodb://localhost:27017/productServiceTest")
class ProductServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
