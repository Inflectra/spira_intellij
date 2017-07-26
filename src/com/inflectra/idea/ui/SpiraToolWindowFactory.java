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

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import com.inflectra.idea.core.SpiraTeamCredentials;
import com.inflectra.idea.core.SpiraTeamUtil;
import com.inflectra.idea.core.model.Artifact;
import com.inflectra.idea.core.model.Incident;
import com.inflectra.idea.core.model.Requirement;
import com.inflectra.idea.core.model.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpiraToolWindowFactory implements ToolWindowFactory {
  /**
   * Contains all of the assigned artifacts
   */
  private JBPanel topPanel;
  /**
   * Contains information about the currently selected artifact
   */
  private JBPanel bottomPanel;

  public SpiraToolWindowFactory() {
    topPanel = new JBPanel();
    topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
    //make the panel lay out its children vertically, instead of horizontally
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

    bottomPanel = new JBPanel();
    bottomPanel.setBorder(new EmptyBorder(5,10,5,10));
    //make the panel lay out its children vertically, instead of horizontally
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
  }

  /**
   * Adds all requirements to {@code topPanel}
   */
  private void addRequirements(SpiraTeamCredentials credentials) throws IOException {
    JBLabel requirementsLabel = new JBLabel("<HTML><h2>Requirements</h2></HTML>");
    topPanel.add(requirementsLabel);
    JBPanel requirements = new JBPanel();
    requirements.setBorder(new EmptyBorder(0, 10, 0, 0));
    requirements.setLayout(new BoxLayout(requirements, BoxLayout.Y_AXIS));
    topPanel.add(requirements);

    Gson gson = new Gson();
    //get JSON from an HTTP request
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedRequirements(credentials)));
    //read the JSON coming from the HTTP request
    ArrayList<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    for (LinkedTreeMap map : list) {
      int projectId = ((Double)map.get("ProjectId")).intValue();
      int artifactId = ((Double)map.get("RequirementId")).intValue();
      String description = (String)map.get("Description");
      String projectName = (String)map.get("ProjectName");
      //the name of the artifact
      String name = (String)map.get("Name");
      //create an artifact with the fields from above
      Artifact artifact = new Incident(projectId, projectName, artifactId, name);
      //set the description of the artifact
      artifact.setDescription(description);
      JBLabel label = new JBLabel(name);
      label.addMouseListener(new LabelMouseListener(artifact, credentials.getUrl(), label, this));
      requirements.add(label);
    }
    requirementsLabel.addMouseListener(new TreeListener(requirements));
  }

  /**
   * Adds all tasks to {@code topPanel}
   */
  private void addTasks(SpiraTeamCredentials credentials) throws IOException {
    JBLabel tasksLabel = new JBLabel("<HTML><h2>Tasks</h2></HTML>");
    topPanel.add(tasksLabel);
    JBPanel tasks = new JBPanel();
    tasks.setBorder(new EmptyBorder(0, 10, 0, 0));
    tasks.setLayout(new BoxLayout(tasks, BoxLayout.Y_AXIS));
    topPanel.add(tasks);

    Gson gson = new Gson();
    //get JSON from an HTTP request
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedTasks(credentials)));
    //read the JSON coming from the HTTP request
    ArrayList<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    for (LinkedTreeMap map : list) {
      int projectId = ((Double)map.get("ProjectId")).intValue();
      int artifactId = ((Double)map.get("TaskId")).intValue();
      String description = (String)map.get("Description");
      String projectName = (String)map.get("ProjectName");
      //the name of the artifact
      String name = (String)map.get("Name");
      //create an artifact with the fields from above
      Artifact artifact = new Incident(projectId, projectName, artifactId, name);
      //set the description
      artifact.setDescription(description);
      JBLabel label = new JBLabel(name);
      label.addMouseListener(new LabelMouseListener(artifact, credentials.getUrl(), label, this));
      tasks.add(label);
    }
    tasksLabel.addMouseListener(new TreeListener(tasks));
  }

  /**
   * Adds all incidents to {@code topPanel}
   */
  private void addIncidents(SpiraTeamCredentials credentials) throws IOException {
    JBLabel incidentsLabel = new JBLabel("<HTML><h2>Incidents</h2></HTML>");
    Font font = incidentsLabel.getFont();
    topPanel.add(incidentsLabel);
    JBPanel incidents = new JBPanel();
    incidents.setBorder(new EmptyBorder(0, 10, 0, 0));
    incidents.setLayout(new BoxLayout(incidents, BoxLayout.Y_AXIS));
    topPanel.add(incidents);

    Gson gson = new Gson();
    //read the JSON coming from the HTTP request
    ArrayList<LinkedTreeMap> list = SpiraTeamUtil.getAssignedIncidents(credentials);
    for (LinkedTreeMap map : list) {
      int projectId = ((Double)map.get("ProjectId")).intValue();
      int artifactId = ((Double)map.get("IncidentId")).intValue();
      String description = (String)map.get("Description");
      String projectName = (String)map.get("ProjectName");
      //the name of the artifact
      String name = (String)map.get("Name");
      //create an artifact with the fields from above
      Artifact artifact = new Incident(projectId, projectName, artifactId, name);
      //set the description
      artifact.setDescription(description);
      JBLabel label = new JBLabel(name);
      label.addMouseListener(new LabelMouseListener(artifact, credentials.getUrl(), label, this));
      incidents.add(label);
    }
    incidentsLabel.addMouseListener(new TreeListener(incidents));
  }

  /**
   * Show information in the bottom panel about the provided artifact
   * @param artifact The artifact to show information about
   */
  public void showInformation(Artifact artifact) {
    //removes everything currently stored in the bottomPanel
    bottomPanel.removeAll();
    //show the name of the artifact as the title of the bottom panel
    JBLabel title = new JBLabel("<HTML><h2>" + artifact.getName() + "</h2></HTML>");
    bottomPanel.add(title);
    //need to show the changes
    bottomPanel.updateUI();

  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow window) {
    SpiraTeamCredentials credentials = SpiraTeamCredentials.loadCredentials();
    //SpiraTeamCredentials credentials = ServiceManager.getService(SpiraTeamCredentials.class);
    System.out.println(credentials);
    try {
      addRequirements(credentials);
      addTasks(credentials);
      addIncidents(credentials);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    //enable scrolling
    JBScrollPane topScroll = new JBScrollPane(topPanel);
    //enables the split screen
    JBSplitter splitter = new JBSplitter();
    //make the splitter divide horizontally
    splitter.setOrientation(true);
    //having the top panel be...on top
    splitter.setFirstComponent(topScroll);
    //enable scrolling
    JBScrollPane bottomScroll = new JBScrollPane(bottomPanel);
    //have the bottom panel be on the bottom
    splitter.setSecondComponent(bottomScroll);
    //add the split-screen to the tool window
    window.getComponent().add(splitter);

  }

  @Override
  public void init(ToolWindow window) {
  }

  @Override
  public boolean shouldBeAvailable(@NotNull Project project) {
    return false;
  }

  @Override
  public boolean isDoNotActivateOnStart() {
    return false;
  }
}

/**
 * Adds functionality for creating custom trees
 */
class TreeListener implements MouseListener {
  JBPanel panel;
  boolean isExpanded = false;

  public TreeListener(JBPanel panel) {
    this.panel = panel;
    //make panel invisible by default
    panel.setVisible(false);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (isExpanded) {
      //hide the artifacts
      panel.setVisible(false);
      isExpanded = false;
    }
    else {
      //show the artifacts
      panel.setVisible(true);
      isExpanded = true;
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {

  }

  @Override
  public void mouseReleased(MouseEvent e) {

  }

  @Override
  public void mouseEntered(MouseEvent e) {

  }

  @Override
  public void mouseExited(MouseEvent e) {

  }
}

/**
 * Listener which implements link functionality to label's
 */
class LabelMouseListener implements MouseListener {
  private Artifact artifact;
  private String url;
  private JBLabel label;
  private SpiraTeamPopup popup;
  private SpiraToolWindowFactory window;

  public LabelMouseListener(Artifact artifact, String url, JBLabel label, SpiraToolWindowFactory window) {
    this.artifact = artifact;
    this.url = url;
    this.label = label;
    this.window = window;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    //open artifact in browser
    //SpiraTeamUtil.openURL(SpiraTeamUtil.getArtifactURI(artifact, url));
    window.showInformation(artifact);
  }

  @Override
  public void mousePressed(MouseEvent e) {

  }

  @Override
  public void mouseReleased(MouseEvent e) {

  }

  @Override
  public void mouseEntered(MouseEvent e) {
    Font font = label.getFont();
    Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
    //turning on the underline
    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    //setting the new font
    label.setFont(font.deriveFont(attributes));
    //set the cursor to the hand
    label.setCursor(new Cursor(Cursor.HAND_CURSOR));

    //popup = new SpiraTeamPopup(createPanel(), label, artifact);
  }

  /**
   * @return A JBPanel with information regarding to the current artifact
   */
  private JBPanel createPanel() {
    JBPanel panel = new JBPanel();
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    //contains the artifact prefix and ID as well as the project
    JBLabel title = new JBLabel(artifact.getPrefix() + ":" + artifact.getArtifactId() + "   Project: " + artifact.getProjectName());
    panel.add(title);
    //contains the description, wrapped in HTML as Description supports rich text
    panel.add(new JBLabel("<HTML>Description: " + artifact.getDescription() + "</HTML>"));
    return panel;

  }

  @Override
  public void mouseExited(MouseEvent e) {
    Font font = label.getFont();
    Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
    //-1 is the constant for no underline
    attributes.put(TextAttribute.UNDERLINE, -1);
    //setting the new font
    label.setFont(font.deriveFont(attributes));
    //set the cursor back to normal
    label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }
}