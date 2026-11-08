package com.chitcircle.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private UUID memberId;
    private String memberName;
    private Integer memberNumber;
    private Integer auctionMonth;
    private BigDecimal amount;
    private Instant paymentDate;
    private String paymentMethod;
    private String referenceNumber;
    private String status;
}

