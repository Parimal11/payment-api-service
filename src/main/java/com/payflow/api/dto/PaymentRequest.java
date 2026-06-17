package com.payflow.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    @NotNull
    private Long userId;

    @NotNull
    @Min(1)
    private Long amount;

    @NotBlank
    @Size(min = 3, max = 3 , message = "Currency must be a 3-letter ISO code")
    private String currency;

}