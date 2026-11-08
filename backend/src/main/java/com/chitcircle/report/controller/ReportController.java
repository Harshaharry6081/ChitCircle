package com.chitcircle.report.controller;

import com.chitcircle.common.response.ApiResponse;
import com.chitcircle.report.dto.GroupSummaryReport;
import com.chitcircle.report.dto.MemberStatementReport;
import com.chitcircle.report.service.ReportApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportApplicationService reportService;

    @GetMapping("/group-summary")
    @PreAuthorize("@groupSecurity.canAccessGroup(authentication, #groupId)")
    public ResponseEntity<ApiResponse<GroupSummaryReport>> getGroupSummary(@RequestParam UUID groupId) {
        GroupSummaryReport response = reportService.getGroupSummary(groupId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/member-statement")
    public ResponseEntity<ApiResponse<MemberStatementReport>> getMemberStatement(@RequestParam UUID memberId) {
        MemberStatementReport response = reportService.getMemberStatement(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

