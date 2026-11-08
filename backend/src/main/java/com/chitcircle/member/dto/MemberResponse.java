package com.chitcircle.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private UUID userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Integer memberNumber;
    private String status;
    private Boolean prizeReceived;
    private Instant joinedAt;
}

