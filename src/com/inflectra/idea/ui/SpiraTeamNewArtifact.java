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
package com.inflectra.idea.ui;

import com.inflectra.idea.core.SpiraTeamCredentials;
import com.inflectra.idea.core.SpiraTeamUtil;
import com.inflectra.idea.core.model.ArtifactType;
import com.inflectra.idea.core.model.SpiraTeamProject;
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
import java.util.ArrayList;

/**
 * Description here
 * @author Peter Geertsema
 */
public class SpiraTeamNewArtifact extends DialogWrapper {
  /**
   * The type of artifact being created. Task by default
   */
  ArtifactType type = ArtifactType.REQUIREMENT;
  SpiraTeamCredentials credentials;

  public SpiraTeamNewArtifact(Project project, SpiraTeamCredentials credentials) {
    super(project);
    this.credentials = credentials;
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
    //get the projects available to the current user
    ArrayList<SpiraTeamProject> availableProjectsList = SpiraTeamUtil.getAvailableProjects(credentials);
    //array necessary to pass into the combo box
    SpiraTeamProject[] availableProjectsArray = new SpiraTeamProject[availableProjectsList.size()];
    //have availableProjectsArray mirror that of availableProjectsList
    for(int i=0; i<availableProjectsList.size(); i++) {
      availableProjectsArray[i] = availableProjectsList.get(i);
    }
    //create the combo box
    ComboBox<SpiraTeamProject> projects = new ComboBox<>(availableProjectsArray);
    projects.addActionListener(l -> {
      SpiraTeamProject selectedItem = (SpiraTeamProject) projects.getSelectedItem();
      System.out.println(selectedItem);
    });
    out.add(projects);

    //panel which will contain the fields the user has to put in about the given artifact type and project
    JBPanel fields = new JBPanel();
    //have the panel lay out its children vertically
    BoxLayout layout = new BoxLayout(fields, BoxLayout.Y_AXIS);
    fields.setLayout(layout);
    fields.setAlignmentX(0);
    fields.setBorder(new EmptyBorder(0,0,0,0));
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
    JBTextField name = new JBTextField("Name");
    panel.add(name);
    JBTextField requirementType = new JBTextField("RequirementTypeId");
    panel.add(requirementType);
    panel.updateUI();
  }

  private void addTask(JBPanel panel) {
    panel.removeAll();
    panel.add(new JBLabel("Task"));
    panel.add(new JBLabel("NONSPODFB"));
    panel.updateUI();
  }

  private void addIncident(JBPanel panel) {
    panel.removeAll();
    panel.add(new JBLabel("Incident"));
    panel.add(new JBLabel("ljgsiybsdff"));
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
    //do stuff
    super.doOKAction();
  }

}
