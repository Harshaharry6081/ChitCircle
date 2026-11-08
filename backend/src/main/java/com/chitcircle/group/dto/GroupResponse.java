package com.chitcircle.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private UUID id;
    private String name;
    private BigDecimal chitValue;
    private BigDecimal monthlyContribution;
    private Integer totalMembers;
    private Integer durationMonths;
    private LocalDate startDate;
    private String status;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;
    private int currentMemberCount;
}

