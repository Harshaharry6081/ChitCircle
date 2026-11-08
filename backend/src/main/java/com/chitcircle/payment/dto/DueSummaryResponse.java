package com.chitcircle.payment.dto;

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
public class DueSummaryResponse {
    private UUID memberId;
    private String memberName;
    private Integer memberNumber;
    private Integer auctionMonth;
    private BigDecimal monthlyContribution;
    private BigDecimal dividendCredit;
    private BigDecimal netPayable;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private boolean isPaid;
}

