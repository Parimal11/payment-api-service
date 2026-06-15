package com.payflow.api.dto;


import com.payflow.api.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long userId;
    private Long amount;
    private Status status;
}