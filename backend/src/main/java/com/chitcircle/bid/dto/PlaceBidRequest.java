package com.chitcircle.bid.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class PlaceBidRequest {
    private UUID memberId; // If null, inferred from authenticated user

    @NotNull(message = "Bid discount amount is required")
    @DecimalMin(value = "0.00", message = "Bid discount cannot be negative")
    private BigDecimal bidAmount;
}

