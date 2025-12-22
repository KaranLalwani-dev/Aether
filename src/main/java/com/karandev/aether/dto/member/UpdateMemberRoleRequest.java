package com.karandev.aether.dto.member;

import com.karandev.aether.enums.ProjectRole;

public record UpdateMemberRoleRequest(
        ProjectRole role
) {
}
