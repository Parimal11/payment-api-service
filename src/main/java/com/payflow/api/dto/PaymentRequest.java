package com.payflow.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentRequest {

    @NotNull
    private Long userId;

    @NotNull
    @Min(1)
    private Long amount;

}