package com.anju.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThrottleStatusResponse {
    private boolean uploadAllowed;
    private long uploadUsedBytes;
    private long uploadLimitBytes;
    private long uploadRemainingBytes;
    private boolean downloadAllowed;
    private long downloadUsedBytes;
    private long downloadLimitBytes;
    private long downloadRemainingBytes;
}
