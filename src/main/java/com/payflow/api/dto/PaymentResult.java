package com.payflow.api.dto;

import com.payflow.api.dto.PaymentResponse;


public record PaymentResult(PaymentResponse response, boolean isNew) {
}