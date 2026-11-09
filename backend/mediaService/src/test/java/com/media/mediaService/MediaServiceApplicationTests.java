package com.media.mediaService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "MONGODB_URI=mongodb://localhost:27017/mediaServiceTest",
        "CLOUDINARY_API_KEY=test-api-key",
        "CLOUDINARY_API_SECRET=test-api-secret",
        "spring.kafka.listener.auto-startup=false",
        "eureka.client.enabled=false"
})
class MediaServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}
