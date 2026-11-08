package com.chitcircle.report.dto;

import com.chitcircle.ledger.dto.LedgerTransactionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatementReport {
    private UUID memberId;
    private String memberName;
    private Integer memberNumber;
    private UUID groupId;
    private String groupName;
    private Boolean prizeReceived;
    private BigDecimal totalContributions;
    private BigDecimal totalDividendsEarned;
    private BigDecimal prizeAmountWon;
    private BigDecimal netFinancialPosition;
    private List<LedgerTransactionResponse> transactions;
}

