package com.chitcircle.ledger.controller;

import com.chitcircle.common.response.ApiResponse;
import com.chitcircle.ledger.dto.LedgerBalanceResponse;
import com.chitcircle.ledger.service.LedgerApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerApplicationService ledgerService;

    @GetMapping("/groups/{groupId}/ledger")
    @PreAuthorize("@groupSecurity.canAccessGroup(authentication, #groupId)")
    public ResponseEntity<ApiResponse<LedgerBalanceResponse>> getGroupLedger(@PathVariable UUID groupId) {
        LedgerBalanceResponse response = ledgerService.getGroupLedger(groupId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/members/{memberId}/ledger")
    public ResponseEntity<ApiResponse<LedgerBalanceResponse>> getMemberLedger(@PathVariable UUID memberId) {
        LedgerBalanceResponse response = ledgerService.getMemberLedger(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

