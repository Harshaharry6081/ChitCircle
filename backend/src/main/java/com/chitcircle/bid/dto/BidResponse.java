package com.chitcircle.bid.dto;

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
public class BidResponse {
    private UUID id;
    private UUID auctionId;
    private UUID memberId;
    private String memberName;
    private Integer memberNumber;
    private BigDecimal bidAmount;
    private String status;
    private Instant submittedAt;
    private boolean isHighest;
}

