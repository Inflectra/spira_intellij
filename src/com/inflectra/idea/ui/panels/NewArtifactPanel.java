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
import com.inflectra.idea.core.model.SpiraTeamPriority;
import com.inflectra.idea.core.model.SpiraTeamUser;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * The general panel used in the SpiraTeamNewArtifact class to create artifacts
 * @author Peter Geertsema
 */
public abstract class NewArtifactPanel extends JBPanel {
  protected SpiraTeamCredentials credentials;
  /**
   * The name of the artifact
   */
  protected JBTextField name;
  protected ComboBox<SpiraTeamUser> owner;
  protected ComboBox<SpiraTeamPriority> priority;
  protected int projectId;

  public NewArtifactPanel(SpiraTeamCredentials credentials, int projectId) {
    this.credentials = credentials;
    this.projectId = projectId;
    //make our panel have an empty border and lay out its children vertically
    setBorder(new EmptyBorder(0,0,0,0));
    LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
    setLayout(layout);

    //name of the artifact
    name = new JBTextField("Name");
    add(name);

    //get the active users in the current project
    SpiraTeamUser[] users = SpiraTeamUtil.getProjectUsers(credentials, projectId);
    owner = new ComboBox<>(users);
    //set the default selected user to the one currently signed in
    for(int i=0; i<users.length; i++) {
      //if we find the user, set the default index to that user
      if(users[i].getUsername().equals(credentials.getUsername())) {
        owner.setSelectedIndex(i);
        break;
      }
    }
    add(owner);
  }

  /**
   * @return The name the user has put in
   */
  public String getArtifactName() {
    return name.getText();
  }

  /**
   * @return The currently selected owner of the artifact
   */
  public SpiraTeamUser getSelectedOwner() {
    return (SpiraTeamUser) owner.getSelectedItem();
  }

  /**
   * @return The priority the user has selected
   */
  public SpiraTeamPriority getSelectedPriority() {
    return (SpiraTeamPriority) priority.getSelectedItem();
  }

  /**
   * @return The ID of the project
   */
  public int getProjectId() {
    return projectId;
  }
}
