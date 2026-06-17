package com.payflow.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.api.model.Status;
import com.payflow.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final PaymentService service;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-results", groupId = "payment-api-group")
    public void consume(String message) throws Exception{

        log.info("Received payment result event: {}", message);

       PaymentResultEvent event = objectMapper.readValue(message, PaymentResultEvent.class);
        

        Status status = Status.valueOf(event.getStatus());
        service.updateTransactionStatus(event.getTransactionId(), status);
    }
}