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

import com.inflectra.idea.core.model.artifacts.ArtifactType;

/**
 * Represents a project role in the system
 * @author Peter Geertsema
 */
public class SpiraTeamProjectRole {
  private int roleId;
  //if one of these can be created
  private boolean canCreateIncident;
  private boolean canCreateTask;
  private boolean canCreateRequirement;

  public SpiraTeamProjectRole() {
  }


  public void setRoleId(int r) {
    roleId = r;
  }
  public int getRoleId() {
    return roleId;
  }
  public void setCanCreateIncident(boolean in) {
    canCreateIncident = in;
  }
  public void setCanCreateTask(boolean in) {
    canCreateTask = in;
  }
  public void setCanCreateRequirement(boolean in) {
    canCreateRequirement = in;
  }
  public boolean canCreateIncident() {
    return canCreateIncident;
  }
  public boolean canCreateTask() {
    return canCreateTask;
  }
  public boolean canCreateRequirement() {
    return canCreateRequirement;
  }

  /**
   * Checks if the user can create an artifact of the given type
   * @return True, if possible; false otherwise
   */
  public boolean canCreate(ArtifactType type) {
    if(type == ArtifactType.TASK)
      return canCreateTask;
    else if(type == ArtifactType.INCIDENT)
      return canCreateIncident;
    else if(type == ArtifactType.REQUIREMENT)
      return canCreateRequirement;
    return false;
  }

  /**
   * @return The possible types of artifacts
   */
  public ArtifactType[] getPossibleArtifactTypes() {
    //the length of the return array
    int arrLength = 1;
    //finding the length of the return array
    if(canCreateRequirement)
      arrLength++;
    if(canCreateTask)
      arrLength++;
    if(canCreateIncident)
      arrLength++;
    //the array we will return
    ArtifactType[] out = new ArtifactType[arrLength];
    //exists no matter what
    out[0] = ArtifactType.PLACERHOLDER;
    //the index to store the type in
    int count = 1;
    if(canCreateRequirement) {
      out[count] = ArtifactType.REQUIREMENT;
      count++;
    }
    if(canCreateTask) {
      out[count] = ArtifactType.TASK;
      count++;
    }
    if(canCreateIncident) {
      out[count] = ArtifactType.INCIDENT;
    }
    return out;
  }
}
