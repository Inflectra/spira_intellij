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
  private String projectName;
  /**
   * The name of the artifact
   */
  private String name;
  /**
   * The ID of the artifact
   */
  private int artifactId;
  private ArtifactType artifactType;
  private String description;

  public Artifact(int projectId, String projectName, int artifactId, ArtifactType artifactType, String name) {
    this.projectId = projectId;
    this.artifactId = artifactId;
    this.artifactType = artifactType;
    this.name = name;
    this.projectName = projectName;
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
   * @return The description of the artifact
   */
  public String getDescription() {
    if(description == null)
      return "none";
    return description;
  }

  /**
   * @return The name of the artifact
   */
  public String getName() {
    return name;
  }

  /**
   * @param description The new value for the description property
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return The value of the projectName property
   */
  public String getProjectName() {
    return projectName;
  }

  /**
   * @return The prefix for the artifact type, example IN for Incidents
   */
  public String getPrefix() {
    return artifactType.getPrefix();
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

  @Override
  public boolean equals(Object other) {
    if(!(other instanceof Artifact))
      return false;
    Artifact artifact = (Artifact)other;
    if(artifact.projectId == projectId && artifact.artifactId == artifactId && artifact.artifactType == artifactType)
      return true;
    return false;
  }
}
