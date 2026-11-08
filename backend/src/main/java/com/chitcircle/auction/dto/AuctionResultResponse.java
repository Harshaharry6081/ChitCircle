package com.chitcircle.auction.dto;

import com.chitcircle.bid.dto.BidResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionResultResponse {
    private UUID auctionId;
    private UUID groupId;
    private String groupName;
    private Integer monthNumber;
    private Instant auctionDate;
    private BigDecimal totalPool;
    private BigDecimal winningDiscount;
    private BigDecimal prizeAmount;
    private BigDecimal dividendPool;
    private BigDecimal dividendPerMember;
    private UUID winnerMemberId;
    private String winnerMemberName;
    private Integer winnerMemberNumber;
    private String status;
    private List<BidResponse> bids;
}

