package com.karandev.aether.mapper;

import com.karandev.aether.dto.project.FileNode;
import com.karandev.aether.entity.ProjectFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

    List<FileNode> toListOfFileNode(List<ProjectFile> projectFiles);
}
