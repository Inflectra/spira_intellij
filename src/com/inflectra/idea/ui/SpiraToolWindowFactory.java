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
import com.intellij.ui.treeStructure.PatchedDefaultMutableTreeNode;
import com.intellij.ui.treeStructure.Tree;
import com.sun.javafx.tk.Toolkit;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
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
  private Tree incidents;
  private Tree requirements;
  private Tree tasks;
  private Tree artifacts;

  public SpiraToolWindowFactory() {
    panel = new JBPanel();
    panel.setBorder(new EmptyBorder(5, 10, 5, 10));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
  }

  /**
   * Looks through the JSON Provided in the reader and uses that data to show data
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
    for(LinkedTreeMap map: list) {
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
   * Adds all requirements to the {@code requirements} Tree
   */
  private void addRequirements(SpiraTeamCredentials credentials) throws IOException {
    PatchedDefaultMutableTreeNode node = new PatchedDefaultMutableTreeNode("My Assigned Requirements");
    requirements = new Tree(node);
    requirements.addTreeSelectionListener(new SpiraTreeSelectionListener(requirements));

    Gson gson = new Gson();
    //get JSON from an HTTP request
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedRequirements(credentials)));
    //read the JSON coming from the HTTP request
    ArrayList<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    for(LinkedTreeMap map: list) {
      int projectId = ((Double)map.get("ProjectId")).intValue();
      int artifactId = ((Double)map.get("RequirementId")).intValue();
      Artifact artifact = new Requirement(projectId, artifactId);
      SpiraTreeNode requirementNode = new SpiraTreeNode(map.get("Name"), SpiraTeamUtil.getArtifactURI(artifact, credentials.getUrl()));
      node.add(requirementNode);
    }
    //setting width to maximum to avoid text clipping
    Dimension dimension = requirements.getMaximumSize();
    dimension.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
    requirements.setMaximumSize(dimension);
  }

  private void addTasks(SpiraTeamCredentials credentials) throws IOException {
    PatchedDefaultMutableTreeNode node = new PatchedDefaultMutableTreeNode("My Assigned Tasks");
    tasks = new Tree(node);
    tasks.addTreeSelectionListener(new SpiraTreeSelectionListener(tasks));

    Gson gson = new Gson();
    //get JSON from an HTTP request
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedTasks(credentials)));
    //read the JSON coming from the HTTP request
    ArrayList<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    for(LinkedTreeMap map: list) {
      int projectId = ((Double)map.get("ProjectId")).intValue();
      int artifactId = ((Double)map.get("TaskId")).intValue();
      Artifact artifact = new Task(projectId, artifactId);
      SpiraTreeNode taskNode = new SpiraTreeNode(map.get("Name"), SpiraTeamUtil.getArtifactURI(artifact, credentials.getUrl()));
      node.add(taskNode);
    }
    //setting width to maximum to avoid text clipping
    Dimension dimension = tasks.getMaximumSize();
    dimension.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
    tasks.setMaximumSize(dimension);
  }

  private void addIncidents(SpiraTeamCredentials credentials) throws IOException {
    PatchedDefaultMutableTreeNode node = new PatchedDefaultMutableTreeNode("My Assigned Incidents");
    incidents = new Tree(node);
    incidents.addTreeSelectionListener(new SpiraTreeSelectionListener(incidents));

    ArrayList<LinkedTreeMap> list = SpiraTeamUtil.getAssignedIncidents(credentials);
    for(LinkedTreeMap map: list) {
      int projectId = ((Double)map.get("ProjectId")).intValue();
      int artifactId = ((Double)map.get("IncidentId")).intValue();
      Artifact artifact = new Incident(projectId, artifactId);
      SpiraTreeNode incidentNode = new SpiraTreeNode(map.get("Name"), SpiraTeamUtil.getArtifactURI(artifact, credentials.getUrl()));
      node.add(incidentNode);
    }
    //incidents.expand
    //setting width to maximum to avoid text clipping
    Dimension dimension = incidents.getMaximumSize();
    dimension.setSize(Integer.MAX_VALUE, dimension.getHeight());
    incidents.setMaximumSize(dimension);
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow window) {
    SpiraTeamCredentials credentials = SpiraTeamCredentials.loadCredentials();
    //SpiraTeamCredentials credentials = ServiceManager.getService(SpiraTeamCredentials.class);
    System.out.println(credentials);
    try {
      addRequirements(credentials);
      panel.add(requirements);
      addTasks(credentials);
      panel.add(tasks);
      addIncidents(credentials);
      panel.add(incidents);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    window.getComponent().add(panel);
  }

  @Override
  public void init(ToolWindow window) {
    System.out.println(panel.getWidth());
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

class SpiraTreeSelectionListener implements TreeSelectionListener {
  private Tree tree;
  public SpiraTreeSelectionListener(Tree tree) {
    this.tree = tree;
  }
  @Override
  public void valueChanged(TreeSelectionEvent e) {
    Object obj = tree.getLastSelectedPathComponent();
    if(obj == null) {
      //nothing is selected
      return;
    }
    if(obj instanceof SpiraTreeNode) {
      //cast to node
      SpiraTreeNode node = (SpiraTreeNode)obj;
      if(node.isLeaf()) {
        //open the page associated with the node
        SpiraTeamUtil.openURL(node.getUri());
      }
    }
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