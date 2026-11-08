package com.chitcircle.member.controller;

import com.chitcircle.common.response.ApiResponse;
import com.chitcircle.member.domain.MemberStatus;
import com.chitcircle.member.dto.AddMemberRequest;
import com.chitcircle.member.dto.MemberResponse;
import com.chitcircle.member.service.MemberApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberApplicationService memberService;

    @PostMapping
    @PreAuthorize("@groupSecurity.isGroupAdmin(authentication, #groupId)")
    public ResponseEntity<ApiResponse<MemberResponse>> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddMemberRequest request
    ) {
        MemberResponse response = memberService.addMember(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Member added to group", response));
    }

    @GetMapping
    @PreAuthorize("@groupSecurity.canAccessGroup(authentication, #groupId)")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> listMembers(@PathVariable UUID groupId) {
        List<MemberResponse> response = memberService.listMembers(groupId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{memberId}")
    @PreAuthorize("@groupSecurity.canAccessGroup(authentication, #groupId)")
    public ResponseEntity<ApiResponse<MemberResponse>> getMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId
    ) {
        MemberResponse response = memberService.getMember(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{memberId}/status")
    @PreAuthorize("@groupSecurity.isGroupAdmin(authentication, #groupId)")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMemberStatus(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @RequestParam MemberStatus status
    ) {
        MemberResponse response = memberService.updateStatus(memberId, status);
        return ResponseEntity.ok(ApiResponse.success("Member status updated", response));
    }
}

