package com.karandev.aether.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberId {

    Long projectId;
    Long userId;
}
