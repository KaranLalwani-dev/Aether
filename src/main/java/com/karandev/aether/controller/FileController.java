package com.karandev.aether.controller;

import com.karandev.aether.dto.project.FileContentResponse;
import com.karandev.aether.dto.project.FileNode;
import com.karandev.aether.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects/{projectId}/files")
public class FileController {

    private ProjectFileService projectFileService;

    @GetMapping
    public ResponseEntity<List<FileNode>> getFileTree(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectFileService.getFileTree(projectId));
    }

    @GetMapping("{*path}")
    public ResponseEntity<FileContentResponse> getFile(@PathVariable Long projectId, @PathVariable String path) {

        return ResponseEntity.ok(projectFileService.getFileContent(projectId, path));
    }
}
