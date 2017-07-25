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
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
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
  private JBPanel panel;
  private JBPanel incidents;
  private JBPanel requirements;
  private JBPanel tasks;

  public SpiraToolWindowFactory() {
    panel = new JBPanel();
    panel.setBorder(new EmptyBorder(5, 10, 5, 10));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
  }

  /**
   * Looks through the JSON Provided in the reader and uses that data to show data
   *
   * @param reader
   * @return
   */
  private JPanel parseJSON(JsonReader reader) {
    JPanel out = new JPanel();
    //make out lay out its components vertically
    out.setLayout(new BoxLayout(out, BoxLayout.Y_AXIS));
    Gson gson = new Gson();
    ArrayList<LinkedTreeMap> list = gson.fromJson(reader, ArrayList.class);
    //loop through every artifact in the JSON
    for (LinkedTreeMap map : list) {
      int projectId = ((Double)map.get("ProjectId")).intValue();
      int artifactId = ((Double)map.get("IncidentId")).intValue();
      Artifact artifact = new Incident(projectId, artifactId);
      //show the name of the artifact
      JBLabel label = new JBLabel((String)map.get("Name"));
      label.addMouseListener(new LabelMouseListener(artifact, "https://demo.spiraservice.net/peter-inflectra", label));

      out.add(label);
    }

    return out;
  }

  /**
   * Adds all requirements to {@code panel}
   */
  private void addRequirements(SpiraTeamCredentials credentials) throws IOException {
    JBLabel requirementsLabel = new JBLabel("<HTML><h2>Requirements</h2></HTML>");
    panel.add(requirementsLabel);
    requirements = new JBPanel();
    requirements.setBorder(new EmptyBorder(0, 10, 0, 0));
    requirements.setLayout(new BoxLayout(requirements, BoxLayout.Y_AXIS));
    panel.add(requirements);

    Gson gson = new Gson();
    //get JSON from an HTTP request
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedRequirements(credentials)));
    //read the JSON coming from the HTTP request
    ArrayList<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    for (LinkedTreeMap map : list) {
      int projectId = ((Double)map.get("ProjectId")).intValue();
      int artifactId = ((Double)map.get("RequirementId")).intValue();
      Artifact artifact = new Requirement(projectId, artifactId);
      JBLabel label = new JBLabel((String)map.get("Name"));
      label.addMouseListener(new LabelMouseListener(artifact, credentials.getUrl(), label));
      requirements.add(label);
    }
    requirementsLabel.addMouseListener(new TreeListener(requirements));
  }

  /**
   * Adds all tasks to {@code panel}
   */
  private void addTasks(SpiraTeamCredentials credentials) throws IOException {
    JBLabel tasksLabel = new JBLabel("<HTML><h2>Tasks</h2></HTML>");
    panel.add(tasksLabel);
    tasks = new JBPanel();
    tasks.setBorder(new EmptyBorder(0, 10, 0, 0));
    tasks.setLayout(new BoxLayout(tasks, BoxLayout.Y_AXIS));
    panel.add(tasks);

    Gson gson = new Gson();
    //get JSON from an HTTP request
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedTasks(credentials)));
    //read the JSON coming from the HTTP request
    ArrayList<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    for (LinkedTreeMap map : list) {
      int projectId = ((Double)map.get("ProjectId")).intValue();
      int artifactId = ((Double)map.get("TaskId")).intValue();
      Artifact artifact = new Task(projectId, artifactId);
      JBLabel label = new JBLabel((String)map.get("Name"));
      label.addMouseListener(new LabelMouseListener(artifact, credentials.getUrl(), label));
      tasks.add(label);
    }
    tasksLabel.addMouseListener(new TreeListener(tasks));
  }

  /**
   * Adds all incidents to {@code panel}
   */
  private void addIncidents(SpiraTeamCredentials credentials) throws IOException {
    JBLabel incidentsLabel = new JBLabel("<HTML><h2>Incidents</h2></HTML>");
    Font font = incidentsLabel.getFont();
    panel.add(incidentsLabel);
    incidents = new JBPanel();
    incidents.setBorder(new EmptyBorder(0, 10, 0, 0));
    incidents.setLayout(new BoxLayout(incidents, BoxLayout.Y_AXIS));
    panel.add(incidents);

    Gson gson = new Gson();
    //read the JSON coming from the HTTP request
    ArrayList<LinkedTreeMap> list = SpiraTeamUtil.getAssignedIncidents(credentials);
    for (LinkedTreeMap map : list) {
      int projectId = ((Double)map.get("ProjectId")).intValue();
      int artifactId = ((Double)map.get("IncidentId")).intValue();
      Artifact artifact = new Incident(projectId, artifactId);
      JBLabel label = new JBLabel((String)map.get("Name"));
      label.addMouseListener(new LabelMouseListener(artifact, credentials.getUrl(), label));
      incidents.add(label);
    }
    incidentsLabel.addMouseListener(new TreeListener(incidents));
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
    window.getComponent().add(panel);
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

  public LabelMouseListener(Artifact artifact, String url, JBLabel label) {
    this.artifact = artifact;
    this.url = url;
    this.label = label;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    SpiraTeamUtil.openURL(SpiraTeamUtil.getArtifactURI(artifact, url));
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

    new SpiraTeamPopup(createPanel(), label);
  }

  /**
   * @return A JBPanel with information regarding to the current artifact
   */
  private JBPanel createPanel() {
    JBPanel panel = new JBPanel();
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(new JBLabel(label.getText()));
    panel.add(new JBLabel("Artifact Type: " + artifact.getArtifactType()));
    panel.add(new JBLabel("Project Id: " + artifact.getProjectId()));

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