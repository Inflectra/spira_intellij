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
import com.inflectra.idea.core.model.SpiraTeamUser;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;

import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel used in SpiraTeamNewArtifact which stores the fields necessary to create a new requirement
 * @author Peter Geertsema
 */
public class NewRequirementPanel extends NewArtifactPanel {
  SpiraTeamCredentials credentials;

  public NewRequirementPanel(SpiraTeamCredentials credentials, int projectId, String name, String description) {
    super(credentials, projectId, name, description);
    this.credentials = credentials;

    //panel which allows type and priority to be side-by-side
    JBPanel panel = new JBPanel();
    panel.setAlignmentX(0);
    panel.setBorder(new EmptyBorder(0, 0, 0, 0));
    LayoutManager layout = new GridLayout(2,3);
    panel.setLayout(layout);

    //only add stuff if the projectId is valid
    SpiraTeamArtifactType[] requirementTypesArr;
    if(projectId != -1) {
      requirementTypesArr = SpiraTeamUtil.getRequirementTypes(credentials, projectId);
    }
    else {
      requirementTypesArr = new SpiraTeamArtifactType[1];
      requirementTypesArr[0] = new SpiraTeamArtifactType(-1, "Please select a project");
    }
    //only add properties if the user has access to types
    if(requirementTypesArr != null && requirementTypesArr.length > 0) {

      //field for the type ID
      artifactType = new ComboBox<>(requirementTypesArr);
      artifactType.setAlignmentX(0);
      JBLabel requirementTypeLabel = new JBLabel("Type: ");
      requirementTypeLabel.setAlignmentX(0);
      panel.add(requirementTypeLabel);

      //priority of the incident
      SpiraTeamPriority[] priorities;
      if (projectId != -1) {
        priorities = SpiraTeamUtil.getRequirementPriorities();
      }
      else {
        priorities = new SpiraTeamPriority[1];
        priorities[0] = new SpiraTeamPriority(-1, "Please select a project");
      }
      priority = new ComboBox<>(priorities);
      priority.setAlignmentX(0);
      JBLabel priorityLabel = new JBLabel("Priority: ");
      priorityLabel.setAlignmentX(0);
      panel.add(priorityLabel);

      //get the active users in the current project
      SpiraTeamUser[] users;
      if (projectId != -1) {
        users = SpiraTeamUtil.getProjectUsers(credentials, projectId);
      }
      else {
        users = new SpiraTeamUser[1];
        users[0] = new SpiraTeamUser("Please select a project", -1, "");
      }
      owner = new ComboBox<>(users);
      owner.setAlignmentX(0);
      //set the default selected user to the one currently signed in
      for (int i = 0; i < users.length; i++) {
        //if we find the user, set the default index to that user
        if (users[i].getUsername().equals(credentials.getUsername())) {
          owner.setSelectedIndex(i);
          break;
        }
      }
      JBLabel ownerLabel = new JBLabel("Owner: ");
      ownerLabel.setAlignmentX(0);
      panel.add(ownerLabel);

      panel.add(artifactType);
      panel.add(priority);
      panel.add(owner);
      add(panel);
    }
    //if the user cannot create a requirement
    else {
      //remove the name and description fields
      removeAll();
      JBLabel label = new JBLabel("You cannot create requirements in this project");
      //add the label to the panel
      add(label);
    }
  }

  public NewRequirementPanel(SpiraTeamCredentials credentials, int projectId) {
    this(credentials, projectId, "", "");
  }

}
