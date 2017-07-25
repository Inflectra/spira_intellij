/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inflectra.idea.core.model;

public abstract class Artifact {
  private int projectId;
  private int artifactId;
  private ArtifactType artifactType;

  public Artifact(int projectId, int artifactId, ArtifactType artifactType) {
    this.projectId = projectId;
    this.artifactId = artifactId;
    this.artifactType = artifactType;
  }

  /**
   * @return The id of the project
   */
  public int getProjectId() {
    return projectId;
  }

  /**
   * @return The id of the artifact
   */
  public int getArtifactId() {
    return artifactId;
  }

  /**
   * @return The type of artifact
   */
  public ArtifactType getArtifactType() {
    return artifactType;
  }

  /**
   * @return True if the artifact is an incident, false otherwise
   */
  public boolean isIncident() {
    return artifactType == ArtifactType.INCIDENT;
  }

  /**
   * @return True if the artifact is a requirement, false otherwise
   */
  public boolean isRequirement() {
    return artifactType == ArtifactType.REQUIREMENT;
  }

  /**
   * @return True if the artifact is a task, false otherwise
   */
  public boolean isTask() {
    return artifactType == ArtifactType.TASK;
  }
}
