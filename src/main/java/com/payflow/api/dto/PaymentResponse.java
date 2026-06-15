package com.payflow.api.dto;


import com.payflow.api.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Long transactionId;
    private Status status;
    private Long amount;
}