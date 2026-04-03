package com.karandev.aether.mapper;

import com.karandev.aether.dto.project.ProjectResponse;
import com.karandev.aether.dto.project.ProjectSummaryResponse;
import com.karandev.aether.entity.Project;
import com.karandev.aether.enums.ProjectRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);

    ProjectSummaryResponse toProjectSummaryResponse(Project project, ProjectRole role);

    List<ProjectSummaryResponse> toListOfProjectSummaryResponse(List<Project> projects);
}
