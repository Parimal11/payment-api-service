package com.payflow.api.kafka;

import lombok.Data;

@Data
public class PaymentResultEvent {
    private Long transactionId;
    private String status;
}