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
import com.inflectra.idea.core.model.SpiraTeamArtifactType;
import com.inflectra.idea.core.model.SpiraTeamPriority;
import com.inflectra.idea.core.model.SpiraTeamUser;
import com.inflectra.idea.core.model.artifacts.ArtifactType;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
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
  protected ComboBox<SpiraTeamArtifactType> artifactType;
  protected SpiraTeamCredentials credentials;
  /**
   * The name of the artifact
   */
  protected JBTextField nameField;
  protected ComboBox<SpiraTeamUser> owner;
  protected ComboBox<SpiraTeamPriority> priority;
  protected SpiraTeamUser[] users;
  protected JTextArea descriptionArea;
  protected int projectId;

  public NewArtifactPanel(SpiraTeamCredentials credentials, int projectId, String name, String description) {
    this.credentials = credentials;
    this.projectId = projectId;
    //make our panel have an empty border and lay out its children vertically
    setBorder(new EmptyBorder(0,0,0,0));
    LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
    setLayout(layout);
    //only add stuff if the projectId is valid

    //name of the artifact
    nameField = new JBTextField(name);
    nameField.setAlignmentX(0);
    JBLabel nameLabel = new JBLabel("Name: ");
    nameLabel.setAlignmentX(0);
    add(nameLabel);
    //make the name not resize vertically
    nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameField.getHeight()));
    add(nameField);
    add(Box.createRigidArea(new Dimension(0, 10)));

    addDescription(description);

    add(Box.createRigidArea(new Dimension(0, 5)));

  }

  /**
   * Utility method called which adds the description label and text area to the pane
   * @param d The description to add
   */
  protected void addDescription(String d) {
    //the description
    JBLabel descriptionLabel = new JBLabel("Description: ");
    descriptionLabel.setAlignmentX(0);
    add(descriptionLabel);
    descriptionArea = new JTextArea(d);
    descriptionArea.setAlignmentX(0);
    descriptionArea.setLineWrap(true);
    add(descriptionArea);
    add(Box.createRigidArea(new Dimension(0,10)));
  }

  /**
   * @return The name the user has put in
   */
  public String getArtifactName() {
    if(nameField != null)
      return nameField.getText();
    return "";
  }

  public SpiraTeamUser[] getUsers() {
    return users;
  }

  public JBTextField getNameField() {
    return nameField;
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

  /**
   * @return The description the user has entered
   */
  public String getDescription() {
    if(descriptionArea != null) {
      return descriptionArea.getText().replace("\t", "");
    }
    return "";
  }

  /**
   * @return The selected type of artifact ex Bug, change request, etc
   */
  public SpiraTeamArtifactType getSelectedArtifactType() {
    return (SpiraTeamArtifactType) artifactType.getSelectedItem();
  }

  public ArtifactType getArtifactType() {
    if(this instanceof  NewIncidentPanel)
      return ArtifactType.INCIDENT;
    else if(this instanceof  NewTaskPanel)
      return ArtifactType.TASK;
    else if(this instanceof NewRequirementPanel)
      return ArtifactType.REQUIREMENT;
    //impossible to get here
    return null;
  }

}
