package com.chitcircle.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupSummaryReport {
    private UUID groupId;
    private String groupName;
    private String status;
    private BigDecimal chitValue;
    private BigDecimal monthlyContribution;
    private Integer totalMembers;
    private Integer enrolledMembers;
    private Integer totalAuctionsConducted;
    private Integer totalAuctionsRemaining;
    private BigDecimal totalPoolCollected;
    private BigDecimal totalPrizeDisbursed;
    private BigDecimal totalDividendsDistributed;
}

