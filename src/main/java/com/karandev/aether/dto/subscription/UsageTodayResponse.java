package com.karandev.aether.dto.subscription;

public record UsageTodayResponse(
    Integer tokensUsed,
    Integer tokensLimit,
    Integer previewsRunning,
    Integer previewsLimit
) {
}
