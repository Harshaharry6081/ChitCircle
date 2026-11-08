package com.chitcircle.auction.controller;

import com.chitcircle.auction.dto.AuctionResponse;
import com.chitcircle.auction.dto.AuctionResultResponse;
import com.chitcircle.auction.dto.CreateAuctionRequest;
import com.chitcircle.auction.service.AuctionApplicationService;
import com.chitcircle.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionApplicationService auctionService;

    @PostMapping("/groups/{groupId}/auctions")
    @PreAuthorize("@groupSecurity.isGroupAdmin(authentication, #groupId)")
    public ResponseEntity<ApiResponse<AuctionResponse>> createAuction(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateAuctionRequest request
    ) {
        AuctionResponse response = auctionService.createAuction(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Auction created successfully", response));
    }

    @GetMapping("/groups/{groupId}/auctions")
    @PreAuthorize("@groupSecurity.canAccessGroup(authentication, #groupId)")
    public ResponseEntity<ApiResponse<List<AuctionResponse>>> listAuctions(@PathVariable UUID groupId) {
        List<AuctionResponse> response = auctionService.listAuctions(groupId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/auctions/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionResponse>> getAuction(@PathVariable UUID auctionId) {
        AuctionResponse response = auctionService.getAuction(auctionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/auctions/{auctionId}/open")
    public ResponseEntity<ApiResponse<AuctionResponse>> openAuction(@PathVariable UUID auctionId) {
        AuctionResponse response = auctionService.openAuction(auctionId);
        return ResponseEntity.ok(ApiResponse.success("Auction opened for bidding", response));
    }

    @PostMapping("/auctions/{auctionId}/close")
    public ResponseEntity<ApiResponse<AuctionResponse>> closeAuction(@PathVariable UUID auctionId) {
        AuctionResponse response = auctionService.closeAuction(auctionId);
        return ResponseEntity.ok(ApiResponse.success("Auction closed", response));
    }

    @PostMapping("/auctions/{auctionId}/declare-winner")
    public ResponseEntity<ApiResponse<AuctionResultResponse>> declareWinner(@PathVariable UUID auctionId) {
        AuctionResultResponse response = auctionService.declareWinner(auctionId);
        return ResponseEntity.ok(ApiResponse.success("Winner declared successfully", response));
    }

    @GetMapping("/auctions/{auctionId}/result")
    public ResponseEntity<ApiResponse<AuctionResultResponse>> getAuctionResult(@PathVariable UUID auctionId) {
        AuctionResultResponse response = auctionService.getAuctionResult(auctionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/auctions/{auctionId}/settle")
    public ResponseEntity<ApiResponse<AuctionResponse>> settleAuction(@PathVariable UUID auctionId) {
        AuctionResponse response = auctionService.settleAuction(auctionId);
        return ResponseEntity.ok(ApiResponse.success("Auction settled successfully. Ledgers updated.", response));
    }
}

