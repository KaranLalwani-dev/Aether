package com.karandev.aether.dto.member;

import com.karandev.aether.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull ProjectRole role
) {
}
