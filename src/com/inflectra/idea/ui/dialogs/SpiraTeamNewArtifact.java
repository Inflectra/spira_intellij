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
import com.inflectra.idea.core.model.artifacts.ArtifactType;
import com.inflectra.idea.core.model.SpiraTeamArtifactType;
import com.inflectra.idea.core.model.SpiraTeamProject;
import com.inflectra.idea.ui.SpiraToolWindowFactory;
import com.inflectra.idea.ui.panels.NewIncidentPanel;
import com.inflectra.idea.ui.panels.NewRequirementPanel;
import com.inflectra.idea.ui.panels.NewTaskPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;

/**
 * The popup which is used to create a new artifact when a user requests it
 * @author Peter Geertsema
 */
public class SpiraTeamNewArtifact extends DialogWrapper {
  /**
   * The type of artifact being created. Task by default
   */
  ArtifactType type = ArtifactType.REQUIREMENT;
  SpiraTeamCredentials credentials;
  Project project;
  /**
   * The ID of the project currently selected by the user
   */
  int projectId;

  private NewRequirementPanel requirementPanel;
  private NewTaskPanel taskPanel;
  private NewIncidentPanel incidentPanel;

  public SpiraTeamNewArtifact(Project project, SpiraTeamCredentials credentials) {
    super(project);
    this.credentials = credentials;
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
    fields.setBorder(new EmptyBorder(0,0,0,0));

    //get the projects available to the current user
    List<SpiraTeamProject> availableProjectsList = SpiraTeamUtil.getAvailableProjects(credentials);
    //array necessary to pass into the combo box
    SpiraTeamProject[] availableProjectsArray = new SpiraTeamProject[availableProjectsList.size()];
    //have availableProjectsArray mirror that of availableProjectsList
    for(int i=0; i<availableProjectsList.size(); i++) {
      availableProjectsArray[i] = availableProjectsList.get(i);
    }
    //set the projectId to the default selected project
    projectId = availableProjectsArray[0].getProjectId();
    //create the combo box
    ComboBox<SpiraTeamProject> projects = new ComboBox<>(availableProjectsArray);
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
    ArtifactType[] types = {ArtifactType.REQUIREMENT, ArtifactType.TASK, ArtifactType.INCIDENT};
    ComboBox<ArtifactType> typeSelection = new ComboBox<>(types);
    //default is requirement
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
    //add the requirement fields
    addRequirement(fields);
    return out;
  }

  /**
   *
   * @param panel The panel to add fields to
   */
  private void addRequirement(JBPanel panel) {
    panel.removeAll();
    //if there is not already a requirement panel or another project has been selected, create one
    if(requirementPanel == null || requirementPanel.getProjectId() != projectId)
      requirementPanel = new NewRequirementPanel(credentials, projectId);
    //add the requirement panel to the dialog
    panel.add(requirementPanel);
    panel.updateUI();
  }

  private void addTask(JBPanel panel) {
    panel.removeAll();
    //if there is not already a task panel or another project has been selected, create one
    if(taskPanel == null || taskPanel.getProjectId() != projectId)
      taskPanel = new NewTaskPanel(credentials, projectId);
    //add the requirement panel to the dialog
    panel.add(taskPanel);
    panel.updateUI();
  }

  private void addIncident(JBPanel panel) {
    panel.removeAll();
    //if there is not already an incident panel or another project has been selected, create one
    if(incidentPanel == null || taskPanel.getProjectId() != projectId)
      incidentPanel = new NewIncidentPanel(credentials, projectId);
    //add the requirement panel to the dialog
    panel.add(incidentPanel);
    panel.updateUI();
  }

  /**
   * Ensures that the information entered is valid
   */
  @Override
  protected ValidationInfo doValidate() {
    //validate
    return null;
  }

  @Override
  protected void doOKAction() {
    if(type == ArtifactType.REQUIREMENT) {
      //create the body of the request
      String body = "{\"Name\": \"" + requirementPanel.getArtifactName() + "\"" +
      ", \"RequirementTypeId\": " + requirementPanel.getSelectedRequirementType().getTypeId() +
      ", \"OwnerId\": " + requirementPanel.getSelectedOwner().getUserId() +
      ", \"ImportanceId\": " + requirementPanel.getSelectedPriority().getPriorityId() +
                    "}";
      //create the requirement in the system
      SpiraTeamUtil.createRequirement(credentials, body, projectId);
    }
    else if(type == ArtifactType.TASK) {
      //create the body of the request
      String body = "{\"Name\": \"" + taskPanel.getArtifactName() + "\"" +
      ", \"TaskTypeId\": " + taskPanel.getSelectedTaskType().getTypeId() +
      ", \"OwnerId\": " + taskPanel.getSelectedOwner().getUserId() +
      ", \"TaskPriorityId\": " + taskPanel.getSelectedPriority().getPriorityId() +
      ", \"TaskStatusId\": 1" +
                    "}";
      //TODO: Add support for different task status ID's
      //create the task in the system
      SpiraTeamUtil.createTask(credentials, body, projectId);
    }
    else if(type == ArtifactType.INCIDENT) {
      //create the body of the request
      String body = "{\"Name\": \"" + incidentPanel.getArtifactName() + "\"" +
      ", \"IncidentTypeId\": " + incidentPanel.getSelectedIncidentType().getTypeId() +
      ", \"Description\": \"" + incidentPanel.getDescription() + "\"" +
      ", \"OwnerId\": " + incidentPanel.getSelectedOwner().getUserId() +
      ", \"PriorityId\": " + incidentPanel.getSelectedPriority().getPriorityId() +
                    "}";
      //create the incident in the system
      SpiraTeamUtil.createIncident(credentials, body, projectId);
    }
    super.doOKAction();
    //refresh the window
    SpiraToolWindowFactory.reload(project);
  }

}
