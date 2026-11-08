package com.chitcircle.ledger.dto;

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
public class LedgerTransactionResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private UUID memberId;
    private String memberName;
    private Integer memberNumber;
    private UUID auctionId;
    private String transactionType;
    private BigDecimal amount;
    private String direction;
    private UUID referenceId;
    private Instant createdAt;
}

