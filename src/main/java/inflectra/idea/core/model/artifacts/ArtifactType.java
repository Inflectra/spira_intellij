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
package inflectra.idea.core.model.artifacts;

/**
 * Enum which contains the different artifact types
 * @author Peter Geertsema
 */
public enum ArtifactType {
  REQUIREMENT("Requirement"),
  INCIDENT("Incident"),
  TASK("Task"),
  PLACERHOLDER("-- Select Type --");

  private String artifactName;

  ArtifactType(String artifactName) {
    this.artifactName = artifactName;
  }

  public String getArtifactName() {
    return artifactName;
  }

  /**
   * @return The prefix of the artifact type, example IN for Incidents
   */
  public String getPrefix() {
    if(this == REQUIREMENT)
      return "RQ";
    if(this == INCIDENT)
      return "IN";
    if(this == TASK)
      return "TK";
    return null;
  }

  @Override
  public String toString() {
    return getArtifactName();
  }
}
