package com.chitcircle.group.controller;

import com.chitcircle.common.response.ApiResponse;
import com.chitcircle.group.dto.CreateGroupRequest;
import com.chitcircle.group.dto.GroupResponse;
import com.chitcircle.group.service.GroupApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupApplicationService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication
    ) {
        GroupResponse response = groupService.createGroup(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Chit group created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> listGroups(Authentication authentication) {
        List<GroupResponse> response = groupService.listGroups(authentication);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("@groupSecurity.canAccessGroup(authentication, #groupId)")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroup(@PathVariable UUID groupId) {
        GroupResponse response = groupService.getGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{groupId}/activate")
    @PreAuthorize("@groupSecurity.isGroupAdmin(authentication, #groupId)")
    public ResponseEntity<ApiResponse<GroupResponse>> activateGroup(@PathVariable UUID groupId) {
        GroupResponse response = groupService.activateGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success("Group activated", response));
    }

    @PostMapping("/{groupId}/close")
    @PreAuthorize("@groupSecurity.isGroupAdmin(authentication, #groupId)")
    public ResponseEntity<ApiResponse<GroupResponse>> closeGroup(@PathVariable UUID groupId) {
        GroupResponse response = groupService.closeGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success("Group closed", response));
    }
}

