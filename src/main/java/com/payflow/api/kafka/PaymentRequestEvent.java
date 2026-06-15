package com.payflow.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentRequestEvent {
    private Long transactionId;
    private Long userId;
    private Long amount;
}