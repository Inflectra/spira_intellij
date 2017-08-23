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
 * Represents a single project in SpiraTeam
 * @author Peter Geertsema
 */
public class SpiraTeamProject {
  private String projectName;
  private int projectId;
  /**
   * The role of the user logged-in
   */
  private SpiraTeamProjectRole userRole;

  public SpiraTeamProject(String projectName, int projectId) {
    this.projectName = projectName;
    this.projectId = projectId;
  }

  public String getProjectName() {
    return projectName;
  }

  public int getProjectId() {
    return projectId;
  }

  public void setUserRole(SpiraTeamProjectRole role) {
    userRole = role;
  }

  public SpiraTeamProjectRole getUserRole() {
    return userRole;
  }

  @Override
  public String toString() {
    return getProjectName();
  }
}
