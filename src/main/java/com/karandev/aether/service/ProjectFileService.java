package com.karandev.aether.service;

import com.karandev.aether.dto.project.FileContentResponse;
import com.karandev.aether.dto.project.FileNode;
import com.karandev.aether.dto.project.FileTreeResponse;

import java.util.List;

public interface ProjectFileService {
    FileTreeResponse getFileTree(Long projectId);

    FileContentResponse getFileContent(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);
}
