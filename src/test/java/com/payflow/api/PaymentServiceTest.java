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
import jakarta.servlet.*;
import jakarta.servlet.ServletException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Test
    void shouldReturnExistingTransaction() {

        TransactionRepository repo = mock(TransactionRepository.class);
        KafkaTemplate<String, String> kafka =
                (KafkaTemplate<String, String>) mock(KafkaTemplate.class);

        PaymentService service =
                new PaymentService(repo, kafka, new ObjectMapper());

        Transaction existing = new Transaction();
        existing.setId(1L);
        existing.setAmount(100L);
        existing.setStatus(Status.PENDING);

        when(repo.findByIdempotencyKey("abc"))
                .thenReturn(Optional.of(existing));

        PaymentRequest req = new PaymentRequest(1L, 100L);

        PaymentResponse response =
                service.createPayment("abc", req);

        assertEquals(1L, response.getTransactionId());

        verify(kafka, never()).send(any(), any(), any());
    }

    @Test
    void shouldCreateNewPaymentAndPublishEvent() throws Exception {

        TransactionRepository repo = mock(TransactionRepository.class);
        KafkaTemplate<String, String> kafka =
                (KafkaTemplate<String, String>) mock(KafkaTemplate.class);

        PaymentService service =
                new PaymentService(repo, kafka, new ObjectMapper());

        when(repo.findByIdempotencyKey("key"))
                .thenReturn(Optional.empty());

        Transaction saved = new Transaction();
        saved.setId(1L);
        saved.setUserId(1L);
        saved.setAmount(100L);
        saved.setStatus(Status.PENDING);

        when(repo.save(any())).thenReturn(saved);

        PaymentRequest req = new PaymentRequest(1L, 100L);

        service.createPayment("key", req);

        verify(kafka, times(1))
                .send(eq("payment-requests"), any(), any());
    }
}
