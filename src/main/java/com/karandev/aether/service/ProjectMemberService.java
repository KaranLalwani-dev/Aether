package com.karandev.aether.service;

import com.karandev.aether.dto.member.InviteMemberRequest;
import com.karandev.aether.dto.member.MemberResponse;
import com.karandev.aether.dto.member.UpdateMemberRoleRequest;

import java.util.List;

public interface ProjectMemberService {

    List<MemberResponse> getProjectMembers(Long projectId);

    MemberResponse inviteMember(Long projectId, InviteMemberRequest request);

    MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request);

    void removeProjectMember(Long projectId, Long memberId);
}
