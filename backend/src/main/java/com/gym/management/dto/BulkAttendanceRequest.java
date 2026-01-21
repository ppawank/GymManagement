package com.gym.management.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkAttendanceRequest {

    @NotNull(message = "Member IDs are required")
    private List<Long> memberIds;
}
