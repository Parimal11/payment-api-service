package com.payflow.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.api.dto.PaymentRequest;
import com.payflow.api.dto.PaymentResponse;

import com.payflow.api.kafka.PaymentRequestEvent;
import com.payflow.api.model.Status;
import com.payflow.api.model.Transaction;
import com.payflow.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public boolean existsByKey(String key) {
        return repo.findByIdempotencyKey(key).isPresent();
    }

    @Transactional
    public PaymentResponse createPayment(String key, PaymentRequest request) {

        Optional<Transaction> existing =
                repo.findByIdempotencyKey(key);

        if (existing.isPresent()) {
            Transaction tx = existing.get();
            return new PaymentResponse(tx.getId(), tx.getStatus(), tx.getAmount());
        }

        Transaction tx = new Transaction();
        tx.setIdempotencyKey(key);
        tx.setUserId(request.getUserId());
        tx.setAmount(request.getAmount());
        tx.setStatus(Status.PENDING);
        tx.setCreatedAt(Instant.now());

        repo.save(tx);

        try {
            PaymentRequestEvent event =
                    new PaymentRequestEvent(tx.getId(), tx.getUserId(), tx.getAmount());

            String payload = objectMapper.writeValueAsString(event);

            String correlationId = MDC.get("correlationId");

            log.info("Publishing payment event for tx={}", tx.getId());

            kafkaTemplate.send("payment-requests", correlationId, payload).whenComplete((result, ex) ->{
                if (ex != null) {
                    log.error("Kafka publish failed for txId={}", tx.getId(), ex);
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }

        return new PaymentResponse(tx.getId(), tx.getStatus(), tx.getAmount());
    }
    public Transaction getTransaction(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public Page<Transaction> list(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.findByUserId(userId, pageable);
    }

    @Transactional
    public void updateTransactionStatus(Long transactionId, Status status) {

        Transaction tx = repo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Idempotency for consumer
        if (tx.getStatus() == Status.SUCCESS) {
            return;
        }

        tx.setStatus(status);
        repo.save(tx);
    }
}