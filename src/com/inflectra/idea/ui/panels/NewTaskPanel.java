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
package com.inflectra.idea.ui.panels;

import com.inflectra.idea.core.SpiraTeamCredentials;
import com.inflectra.idea.core.SpiraTeamUtil;
import com.inflectra.idea.core.model.SpiraTeamArtifactType;
import com.inflectra.idea.core.model.SpiraTeamPriority;
import com.intellij.openapi.ui.ComboBox;

/**
 * Panel used in SpiraTeamNewArtifact which stores the fields necessary to create a new task
 * @author Peter Geertsema
 */
public class NewTaskPanel extends NewArtifactPanel {
  SpiraTeamCredentials credentials;
  ComboBox<SpiraTeamArtifactType> taskType;
  public NewTaskPanel(SpiraTeamCredentials credentials, int projectId) {
    super(credentials, projectId);
    this.credentials = credentials;
    //the types of tasks in the given project
    SpiraTeamArtifactType[] taskTypesArr = SpiraTeamUtil.getTaskTypes(credentials, projectId);
    taskType = new ComboBox<>(taskTypesArr);
    add(taskType);

    //priority of the incident
    SpiraTeamPriority[] priorities = SpiraTeamUtil.getTaskPriorities();
    priority = new ComboBox<>(priorities);
    add(priority);
  }

  /**
   * @return The currently selected task type
   */
  public SpiraTeamArtifactType getSelectedTaskType() {
    return (SpiraTeamArtifactType) taskType.getSelectedItem();
  }
}
