package com.karandev.aether.dto.project;

import java.util.List;

public record FileTreeResponse(
        List<FileNode> files
) {
}
