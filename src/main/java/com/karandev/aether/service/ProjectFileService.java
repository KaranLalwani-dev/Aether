package com.karandev.aether.service;

import com.karandev.aether.dto.project.FileContentResponse;
import com.karandev.aether.dto.project.FileNode;

import java.util.List;

public interface ProjectFileService {
    List<FileNode> getFileTree(Long projectId);

    FileContentResponse getFileContent(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);

}
