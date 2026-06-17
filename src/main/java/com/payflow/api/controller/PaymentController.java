package com.payflow.api.controller;

import com.payflow.api.dto.*;
import com.payflow.api.model.Transaction;
import com.payflow.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.payflow.api.exception.IdempotencyKeyMissingException;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @Valid @RequestBody PaymentRequest request) {

        if (key == null || key.isBlank()) {
            throw new IdempotencyKeyMissingException("Idempotency-Key header is required");
        }

        boolean exists = service.existsByKey(key);
        PaymentResponse response = service.createPayment(key, request);

        if (exists) {
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Already processed", response));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Payment created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> get(@PathVariable Long id) {

        Transaction tx = service.getTransaction(id);

        TransactionResponse response = new TransactionResponse(
                tx.getId(),
                tx.getUserId(),
                tx.getAmount(),
                tx.getStatus(),
                tx.getCurrency()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Transaction fetched", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> list(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Transaction> pageResult = service.list(userId, page, size);

        List<TransactionResponse> data = pageResult.getContent()
                .stream()
                .map(tx -> new TransactionResponse(
                        tx.getId(),
                        tx.getUserId(),
                        tx.getAmount(),
                        tx.getStatus(),
                        tx.getCurrency()
                ))
                .toList();

        PageResponse<TransactionResponse> response =
                new PageResponse<>(
                        data,
                        pageResult.getNumber(),
                        pageResult.getSize(),
                        pageResult.getTotalElements()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Transactions fetched", response));
    }
}