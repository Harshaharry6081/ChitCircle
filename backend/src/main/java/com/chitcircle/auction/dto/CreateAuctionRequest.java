package com.chitcircle.auction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuctionRequest {
    @NotNull(message = "Month number is required")
    @Min(value = 1, message = "Month number must be at least 1")
    private Integer monthNumber;

    @NotNull(message = "Auction date is required")
    private Instant auctionDate;
}

