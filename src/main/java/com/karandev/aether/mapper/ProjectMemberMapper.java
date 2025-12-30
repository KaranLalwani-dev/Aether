package com.karandev.aether.mapper;

import com.karandev.aether.dto.member.MemberResponse;
import com.karandev.aether.entity.ProjectMember;
import com.karandev.aether.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "projectRole", constant = "OWNER")
    MemberResponse toProjectMemberResponseFromOwner(User owner);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "username", source = "user.username")
    MemberResponse toProjectMemberResponseFromMember(ProjectMember projectMember);
}
