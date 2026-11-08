package com.chitcircle.payment.controller;

import com.chitcircle.common.response.ApiResponse;
import com.chitcircle.payment.dto.DueSummaryResponse;
import com.chitcircle.payment.dto.PaymentResponse;
import com.chitcircle.payment.dto.RecordPaymentRequest;
import com.chitcircle.payment.service.PaymentApplicationService;
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
public class PaymentController {

    private final PaymentApplicationService paymentService;

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        PaymentResponse response = paymentService.recordPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Payment recorded successfully", response));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> listPayments(
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) Integer month
    ) {
        List<PaymentResponse> response = paymentService.listPayments(groupId, month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/payments/{paymentId}/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(@PathVariable UUID paymentId) {
        PaymentResponse response = paymentService.confirmPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed", response));
    }

    @PostMapping("/payments/{paymentId}/reverse")
    public ResponseEntity<ApiResponse<PaymentResponse>> reversePayment(
            @PathVariable UUID paymentId,
            @RequestParam(defaultValue = "Manual correction") String reason
    ) {
        PaymentResponse response = paymentService.reversePayment(paymentId, reason);
        return ResponseEntity.ok(ApiResponse.success("Payment reversed", response));
    }

    @GetMapping("/groups/{groupId}/dues")
    @PreAuthorize("@groupSecurity.canAccessGroup(authentication, #groupId)")
    public ResponseEntity<ApiResponse<List<DueSummaryResponse>>> getPendingDues(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "1") int month
    ) {
        List<DueSummaryResponse> response = paymentService.calculatePendingDues(groupId, month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

