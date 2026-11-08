package com.chitcircle.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RecordPaymentRequest {
    @NotNull(message = "Group ID is required")
    private UUID groupId;

    @NotNull(message = "Member ID is required")
    private UUID memberId;

    @NotNull(message = "Auction month is required")
    @Min(value = 1, message = "Month must be at least 1")
    private Integer auctionMonth;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least ₹1.00")
    private BigDecimal amount;

    private Instant paymentDate;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String referenceNumber;
}

