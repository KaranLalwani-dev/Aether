package com.karandev.aether.service;

import com.karandev.aether.dto.deploy.DeployResponse;

public interface DeploymentService {

    DeployResponse deploy(Long projectId);
}
