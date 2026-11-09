package com.media.mediaService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.media.mediaService.model.Media;
import com.media.mediaService.reposiroty.MediaRepository;

@ExtendWith(MockitoExtension.class)
class ProductEventConsumerTest {
    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private ProductEventConsumer consumer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void savesMediaForCreateEvent() throws JsonProcessingException {
        consumer = new ProductEventConsumer(objectMapper, mediaRepository);

        consumer.consumeProduct("""
                {"operation":"CREATE","productId":"product-1","imageUrls":["one.png","two.png"]}
                """);

        verify(mediaRepository).saveAll(List.of(
                new Media(null, "one.png", "product-1"),
                new Media(null, "two.png", "product-1")));
        verify(mediaRepository, never()).deleteByProductId("product-1");
    }

    @Test
    void replacesExistingImagesForUpdateEvent() throws JsonProcessingException {
        consumer = new ProductEventConsumer(objectMapper, mediaRepository);

        consumer.consumeProduct("""
                {"operation":"UPDATE","productId":"product-1","imageUrls":["updated.png"]}
                """);

        verify(mediaRepository).deleteByProductId("product-1");
        verify(mediaRepository).saveAll(List.of(new Media(null, "updated.png", "product-1")));
    }

    @Test
    void deletesExistingImagesAndDoesNotSaveForDeleteEvent() throws JsonProcessingException {
        consumer = new ProductEventConsumer(objectMapper, mediaRepository);

        consumer.consumeProduct("""
                {"operation":"DELETE","productId":"product-1","imageUrls":["old.png"]}
                """);

        verify(mediaRepository).deleteByProductId("product-1");
        verify(mediaRepository, never()).saveAll(org.mockito.ArgumentMatchers.<Iterable<Media>>any());
    }

    @Test
    void doesNotSaveWhenImageUrlsAreMissingOrEmpty() throws JsonProcessingException {
        consumer = new ProductEventConsumer(objectMapper, mediaRepository);
        consumer.consumeProduct("""
                {"operation":"CREATE","productId":"product-1","imageUrls":[]}
                """);
        consumer.consumeProduct("""
                {"operation":"CREATE","productId":"product-2","imageUrls":null}
                """);

        verifyNoInteractions(mediaRepository);
    }

    @Test
    void rejectsMalformedEventJson() {
        consumer = new ProductEventConsumer(objectMapper, mediaRepository);

        assertThrows(JsonProcessingException.class, () -> consumer.consumeProduct("{not-json"));
        verifyNoInteractions(mediaRepository);
    }
}
