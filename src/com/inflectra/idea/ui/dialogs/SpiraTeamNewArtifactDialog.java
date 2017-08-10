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
package com.inflectra.idea.ui.dialogs;

import com.inflectra.idea.core.SpiraTeamCredentials;
import com.inflectra.idea.core.SpiraTeamUtil;
import com.inflectra.idea.core.model.SpiraTeamProject;
import com.inflectra.idea.core.model.artifacts.ArtifactType;
import com.inflectra.idea.ui.SpiraToolWindowFactory;
import com.inflectra.idea.ui.panels.NewArtifactPanel;
import com.inflectra.idea.ui.panels.NewIncidentPanel;
import com.inflectra.idea.ui.panels.NewRequirementPanel;
import com.inflectra.idea.ui.panels.NewTaskPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * The popup which is used to create a new artifact when a user requests it
 * @author Peter Geertsema
 */
public class SpiraTeamNewArtifactDialog extends DialogWrapper {
  /**
   * The type of artifact being created. Placeholder by default
   */
  private ArtifactType type = ArtifactType.PLACERHOLDER;
  private SpiraTeamCredentials credentials;
  private Project project;


  /**
   * The ID of the project currently selected by the user
   */
  private int projectId;

  //panels which store information about their respective artifacts
  /*private NewRequirementPanel requirementPanel;
  private NewTaskPanel taskPanel;
  private NewIncidentPanel incidentPanel;*/
  /**
   * Stores the information about the currently selected artifact type
   */
  private NewArtifactPanel artifactPanel;

  /**
   * Allows users to select which artifact type to create
   */
  private ComboBox<ArtifactType> typeSelection;
  /**
   * Allows users to select which project to create an artifact in
   */
  private ComboBox<SpiraTeamProject> projects;

  public SpiraTeamNewArtifactDialog(Project project, SpiraTeamCredentials credentials) {
    super(project);
    this.credentials = credentials;
    this.setResizable(false);
    this.project = project;
    init();
    setTitle("New Artifact");
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    //the panel to be returned. Contains artifact type and project
    JBPanel out = new JBPanel();
    //have the box lay out its children vertically
    out.setLayout(new BoxLayout(out, BoxLayout.Y_AXIS));
    //make the panel have no border
    out.setBorder(new EmptyBorder(5,5,5,5));
    out.setAlignmentX(0);
    //panel which will contain the fields the user has to put in about the given artifact type and project
    JBPanel fields = new JBPanel();
    //have the panel lay out its children vertically
    BoxLayout layout = new BoxLayout(fields, BoxLayout.Y_AXIS);
    fields.setLayout(layout);
    fields.setAlignmentX(0);
    fields.setBorder(new EmptyBorder(10,0,0,0));

    //get the projects available to the current user
    List<SpiraTeamProject> availableProjectsList = SpiraTeamUtil.getAvailableProjects(credentials);
    //array necessary to pass into the combo box, 1 greater to account for select option
    SpiraTeamProject[] availableProjectsArray = new SpiraTeamProject[availableProjectsList.size() + 1];
    //empty project
    availableProjectsArray[0] = new SpiraTeamProject("-- Select Project --", -1);
    //have availableProjectsArray mirror that of availableProjectsList
    for(int i=0; i<availableProjectsList.size(); i++) {
      availableProjectsArray[i + 1] = availableProjectsList.get(i);
    }
    //set the projectId to the default selected project
    projectId = availableProjectsArray[0].getProjectId();
    //create the combo box
    projects = new ComboBox<>(availableProjectsArray);
    projects.setAlignmentX(0);
    //make the projects ComboBox not resize vertically
    //projects.setMaximumSize(new Dimension(Integer.MAX_VALUE, projects.getHeight()));
    //listener called every time a new option is selected
    projects.addActionListener(l -> {
      SpiraTeamProject selectedItem = (SpiraTeamProject) projects.getSelectedItem();
      projectId = selectedItem.getProjectId();
      //need to refresh the options after another project is selected
      if(type == ArtifactType.REQUIREMENT) {
        addRequirement(fields);
      }
      else if(type == ArtifactType.INCIDENT) {
        addIncident(fields);
      }
      else if(type == ArtifactType.TASK) {
        addTask(fields);
      }
    });
    out.add(projects);

    //the types of artifacts
    ArtifactType[] types = {ArtifactType.PLACERHOLDER, ArtifactType.REQUIREMENT, ArtifactType.TASK, ArtifactType.INCIDENT};
    typeSelection = new ComboBox<>(types);
    typeSelection.setAlignmentX(0);
    //make the typeSelection ComboBox not resize vertically
    //typeSelection.setMaximumSize(new Dimension(Integer.MAX_VALUE,typeSelection.getHeight()));
    //default is placeholder
    typeSelection.setSelectedIndex(0);
    //called every time an option is clicked
    typeSelection.addActionListener(l -> {
      type = (ArtifactType)typeSelection.getSelectedItem();
      if(type == ArtifactType.REQUIREMENT) {
        addRequirement(fields);
      }
      else if(type == ArtifactType.INCIDENT) {
        addIncident(fields);
      }
      else if(type == ArtifactType.TASK) {
        addTask(fields);
      }
    });
    out.add(typeSelection);
    //add the panel with data fields
    out.add(fields);
    //should show fields when the window opens
    addRequirement(fields);

    return out;
  }

  /**
   * Show the fields about requirements
   * @param panel The panel to add fields to
   */
  private void addRequirement(JBPanel panel) {
    panel.removeAll();
    //if there is not already a requirement panel or another project has been selected, create one
    if(artifactPanel == null)
      artifactPanel = new NewRequirementPanel(credentials, projectId);
    else if(artifactPanel.getProjectId() != projectId || artifactPanel.getArtifactType() != ArtifactType.REQUIREMENT){
      String name = artifactPanel.getArtifactName();
      String description = artifactPanel.getDescription();
      artifactPanel = new NewRequirementPanel(credentials, projectId, name, description);
    }
    //add the panel to the dialog
    panel.add(artifactPanel);
    panel.updateUI();
  }

  /**
   * Show the fields about tasks
   * @param panel The panel to add fields to
   */
  private void addTask(JBPanel panel) {
    panel.removeAll();
    //if there is not already a task panel or another project has been selected, create one
    if(artifactPanel == null)
      artifactPanel = new NewTaskPanel(credentials, projectId);
    else if(artifactPanel.getProjectId() != projectId || artifactPanel.getArtifactType() != ArtifactType.TASK){
      String name = artifactPanel.getArtifactName();
      String description = artifactPanel.getDescription();
      artifactPanel = new NewTaskPanel(credentials, projectId, name, description);
    }
    //add the panel to the dialog
    panel.add(artifactPanel);
    panel.updateUI();
  }

  /**
   * Show the fields about incidents
   * @param panel The panel to add fields to
   */
  private void addIncident(JBPanel panel) {
    panel.removeAll();
    //if there is not already a task panel or another project has been selected, create one
    if(artifactPanel == null)
      artifactPanel = new NewIncidentPanel(credentials, projectId);
    else if(artifactPanel.getProjectId() != projectId || artifactPanel.getArtifactType() != ArtifactType.INCIDENT){
      String name = artifactPanel.getArtifactName();
      String description = artifactPanel.getDescription();
      artifactPanel = new NewIncidentPanel(credentials, projectId, name, description);
    }
    //add the panel to the dialog
    panel.add(artifactPanel);
    panel.updateUI();
  }

  /**
   * Ensures that the information entered is valid
   */
  @Override
  protected ValidationInfo doValidate() {
    if(projectId == -1) {
      return new ValidationInfo("You must choose a project", projects);
    }
    else if(type == ArtifactType.PLACERHOLDER) {
      return new ValidationInfo("You must choose a type to create", typeSelection);
    }
    return null;
  }

  /**
   * Called when the user presses OK. This actually adds the artifact to the SpiraTeam system
   */
  @Override
  protected void doOKAction() {
    if(type == ArtifactType.REQUIREMENT) {
      NewRequirementPanel requirementPanel = (NewRequirementPanel) artifactPanel;
      //create the body of the request
      String body = "{\"Name\": \"" + requirementPanel.getArtifactName() + "\"" +
      ", \"RequirementTypeId\": " + requirementPanel.getSelectedArtifactType().getTypeId() +
      ", \"Description\": \"" + requirementPanel.getDescription() + "\"";
      int userId = requirementPanel.getSelectedOwner().getUserId();
      //only add the owner if it is not -1, which is assigned if the user makes no choice
      if(userId != -1) {
        body += ", \"OwnerId\": " + requirementPanel.getSelectedOwner().getUserId();
      }
      int priorityId = requirementPanel.getSelectedPriority().getPriorityId();
      //only add the priority if it is not -1, which is assigned if the user makes no choice
      if(priorityId != -1) {
        body += ", \"ImportanceId\": " + requirementPanel.getSelectedPriority().getPriorityId();
      }

      body += "}";
      //create the requirement in the system
      SpiraTeamUtil.createRequirement(credentials, body, projectId);
    }
    else if(type == ArtifactType.TASK) {
      NewTaskPanel taskPanel = (NewTaskPanel) artifactPanel;
      //create the body of the request
      String body = "{\"Name\": \"" + taskPanel.getArtifactName() + "\"" +
      ", \"TaskTypeId\": " + taskPanel.getSelectedArtifactType().getTypeId() +
      ", \"Description\": \"" + taskPanel.getDescription() + "\"";
      int ownerId = taskPanel.getSelectedOwner().getUserId();
      //only add the owner if it is not -1, which is assigned if the user makes no choice
      if(ownerId != -1) {
        body += ", \"OwnerId\": " + taskPanel.getSelectedOwner().getUserId();
      }
      int priorityId = taskPanel.getSelectedPriority().getPriorityId();
      //only add the priority if it is not -1, which is assigned if the user makes no choice
      if(priorityId != -1) {
        body += ", \"TaskPriorityId\": " + taskPanel.getSelectedPriority().getPriorityId();
      }

      body += ", \"TaskStatusId\": 1";
      //TODO: Add support for different task status ID's
      body += "}";
      //create the task in the system
      SpiraTeamUtil.createTask(credentials, body, projectId);
    }
    else if(type == ArtifactType.INCIDENT) {
      NewIncidentPanel incidentPanel = (NewIncidentPanel) artifactPanel;
      //create the body of the request
      String body = "{\"Name\": \"" + incidentPanel.getArtifactName() + "\"" +
      ", \"IncidentTypeId\": " + incidentPanel.getSelectedArtifactType().getTypeId() +
      ", \"Description\": \"" + incidentPanel.getDescription() + "\"";
      int ownerId = incidentPanel.getSelectedOwner().getUserId();
      //only add the owner if it is not -1, which is assigned if the user makes no choice
      if(ownerId != -1) {
        body += ", \"OwnerId\": " + incidentPanel.getSelectedOwner().getUserId();
      }
      int priorityId = incidentPanel.getSelectedPriority().getPriorityId();
      //only add the priority if it is not -1, which is assigned if the user makes no choice
      if(priorityId != -1) {
        body += ", \"PriorityId\": " + incidentPanel.getSelectedPriority().getPriorityId();
      }
      body+="}";
      //create the incident in the system
      SpiraTeamUtil.createIncident(credentials, body, projectId);
    }
    super.doOKAction();
    //refresh the window
    SpiraToolWindowFactory.reload(project);
  }

}
