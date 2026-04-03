package com.karandev.aether.dto.project;


public record FileNode(
        String path
) {

    @Override
    public String toString() {
        return path;
    }
}
