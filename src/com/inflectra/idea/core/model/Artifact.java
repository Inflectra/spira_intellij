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

/**
 * Class which contains information common to all Artifact types.
 * <p>It is inherited by the Incident, Requirement and Task classes</p>
 * @author peter.geertsema
 */
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
  private String priorityName;
  /**
   * The current workflow status
   */
  private String status;
  /**
   * The type, ex change request, bug, feature, etc
   */
  private String type;

  public Artifact(int projectId, String projectName, int artifactId, ArtifactType artifactType, String name, String priorityName) {
    this.projectId = projectId;
    this.artifactId = artifactId;
    this.artifactType = artifactType;
    this.name = name;
    this.projectName = projectName;
    this.priorityName = priorityName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPriorityName() {
    return priorityName;
  }

  /**
   * @return The current workflow status
   */
  public String getStatus() {
    return status;
  }
  /**
   * @param status The new value for the workflow status property
   */
  public void setStatus(String status) {
    this.status = status;
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
    //not equal of other is not an Artifact
    if(!(other instanceof Artifact))
      return false;
    Artifact artifact = (Artifact)other;
    //they are true if they have the same projectId, artifactId and artifactType
    if(artifact.projectId == projectId && artifact.artifactId == artifactId && artifact.artifactType == artifactType)
      return true;
    return false;
  }
}
