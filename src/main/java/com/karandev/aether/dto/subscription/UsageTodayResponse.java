package com.karandev.aether.dto.subscription;

public record UsageTodayResponse(
    int tokensUsed,
    int tokensLimit,
    int previewsRunning,
    int previewsLimit
) {
}
