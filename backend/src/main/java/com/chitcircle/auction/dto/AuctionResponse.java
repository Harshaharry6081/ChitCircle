package com.chitcircle.auction.dto;

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
public class AuctionResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private Integer monthNumber;
    private Instant auctionDate;
    private BigDecimal totalPool;
    private BigDecimal winningDiscount;
    private BigDecimal prizeAmount;
    private BigDecimal dividendPerMember;
    private UUID winnerMemberId;
    private String winnerMemberName;
    private Integer winnerMemberNumber;
    private String status;
}

