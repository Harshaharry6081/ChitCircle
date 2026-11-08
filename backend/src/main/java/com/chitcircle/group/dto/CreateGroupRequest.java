package com.chitcircle.group.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    private String name;

    @NotNull(message = "Monthly contribution is required")
    @DecimalMin(value = "100.00", message = "Monthly contribution must be at least ₹100")
    private BigDecimal monthlyContribution;

    @NotNull(message = "Total members is required")
    @Min(value = 2, message = "Total members must be at least 2")
    private Integer totalMembers;

    @NotNull(message = "Duration in months is required")
    @Min(value = 2, message = "Duration must be at least 2 months")
    private Integer durationMonths;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
}

