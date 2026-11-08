package com.chitcircle.bid.controller;

import com.chitcircle.bid.dto.BidResponse;
import com.chitcircle.bid.dto.PlaceBidRequest;
import com.chitcircle.bid.service.BidApplicationService;
import com.chitcircle.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auctions/{auctionId}/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidApplicationService bidService;

    @PostMapping
    public ResponseEntity<ApiResponse<BidResponse>> placeBid(
            @PathVariable UUID auctionId,
            @Valid @RequestBody PlaceBidRequest request,
            Authentication authentication
    ) {
        BidResponse response = bidService.placeBid(auctionId, request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Bid placed successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BidResponse>>> listBids(@PathVariable UUID auctionId) {
        List<BidResponse> response = bidService.listBids(auctionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

