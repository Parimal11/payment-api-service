package com.payflow.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.api.dto.PaymentRequest;
import com.payflow.api.dto.PaymentResponse;
import com.payflow.api.model.*;
import com.payflow.api.repository.TransactionRepository;
import com.payflow.api.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture; // Added Import

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Test
    void shouldReturnExistingTransaction() {
        TransactionRepository repo = mock(TransactionRepository.class);
        KafkaTemplate<String, String> kafka =
                (KafkaTemplate<String, String>) mock(KafkaTemplate.class);

        // FIX: Stub kafkaTemplate.send to return a completed future instead of null
        when(kafka.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        PaymentService service =
                new PaymentService(repo, kafka, new ObjectMapper());

        Transaction existing = new Transaction();
        existing.setId(1L);
        existing.setUserId(1L); // Added userId initialization to prevent logging tx=null properties
        existing.setAmount(100L);
        existing.setStatus(Status.PENDING);
        existing.setCurrency("INR");

        when(repo.findByIdempotencyKey("abc"))
                .thenReturn(Optional.of(existing));

        PaymentRequest req = new PaymentRequest(1L, 100L, "INR");

        PaymentResponse response =
                service.createPayment("abc", req);

        assertEquals(1L, response.getTransactionId());
        
        // Note: If your business logic expects a send to happen, 
        // you might need to change never() to times(1) depending on your design.
        verify(kafka, never()).send(any(), any(), any());
    }

    @Test
    void shouldCreateNewPaymentAndPublishEvent() throws Exception {
        TransactionRepository repo = mock(TransactionRepository.class);
        KafkaTemplate<String, String> kafka =
                (KafkaTemplate<String, String>) mock(KafkaTemplate.class);

        // FIX: Stub kafkaTemplate.send to return a completed future instead of null
        when(kafka.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        PaymentService service =
                new PaymentService(repo, kafka, new ObjectMapper());

        when(repo.findByIdempotencyKey("key"))
                .thenReturn(Optional.empty());

        Transaction saved = new Transaction();
        saved.setId(1L);
        saved.setUserId(1L);
        saved.setAmount(100L);
        saved.setStatus(Status.PENDING);
        saved.setCurrency("INR");
        when(repo.save(any())).thenReturn(saved);

        PaymentRequest req = new PaymentRequest(1L, 100L, "INR");

        service.createPayment("key", req);

        verify(kafka, times(1))
                .send(eq("payment-requests"), any(), any());
    }
}