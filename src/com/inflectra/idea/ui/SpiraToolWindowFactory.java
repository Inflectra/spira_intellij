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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Is the 'core' of the plug-in's UI, this is the class from which the SpiraToolWindow originates from
 * @author peter.geertsema
 */
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
   * Performs a REST call and adds all requirements to {@code topPanel}
   */
  private void addRequirements(SpiraTeamCredentials credentials) throws IOException {
    Gson gson = new Gson();
    //getAssignedRequirements returns an InputStream with the JSON from the REST request, that is then read by the JsonReader
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedRequirements(credentials)));
    //Turn the JSON into something java understands
    ArrayList<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    //only show requirements if there are any assigned to the user
    if(list.size() > 0) {
      //title label for requirements
      JBLabel requirementsLabel = new JBLabel("<html><h2>Requirements</h2></html>");
      //add the label to the top panel
      topPanel.add(requirementsLabel);
      //panel which fits under the "Requirements" label which will contain artifact names
      JBPanel requirements = new JBPanel();
      //make the new panel have no border
      requirements.setBorder(new EmptyBorder(0, 10, 0, 0));
      //make the panel lay out its children vertically
      requirements.setLayout(new BoxLayout(requirements, BoxLayout.Y_AXIS));
      //add the new panel to the top panel
      topPanel.add(requirements);
      //loop through every LinkedTreeMap in list
      for (LinkedTreeMap map : list) {
        //get the ProjectId, cast it to a double and get its int value
        int projectId = ((Double)map.get("ProjectId")).intValue();
        int artifactId = ((Double)map.get("RequirementId")).intValue();
        String priorityName = (String)map.get("ImportanceName");
        String description = (String)map.get("Description");
        String projectName = (String)map.get("ProjectName");
        String name = (String)map.get("Name");
        //workflow status name
        String status = (String)map.get("StatusName");
        String type = (String)map.get("RequirementTypeName");


        //create an artifact with the fields from above
        Artifact artifact = new Requirement(projectId, projectName, artifactId, name, priorityName);
        //set the description of the artifact
        artifact.setDescription(description);
        artifact.setStatus(status);
        JBLabel label = new JBLabel(name);
        //allow the user to click the label
        label.addMouseListener(new TopLabelMouseListener(artifact, label, this, credentials.getUrl()));
        requirements.add(label);
      }
      //allow the user to click on the big requirement label to expand/collapse the artifact names
      requirementsLabel.addMouseListener(new TreeListener(requirements, requirementsLabel));
    }
  }

  /**
   * Performs a REST call and adds all tasks to {@code topPanel}
   */
  private void addTasks(SpiraTeamCredentials credentials) throws IOException {
    Gson gson = new Gson();
    //getAssignedRequirements returns an InputStream with the JSON from the REST request, that is then read by the JsonReader
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedTasks(credentials)));
    //Turn the JSON into something java understands
    ArrayList<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    //only add if there are assigned tasks
    if(list.size() > 0) {
      JBLabel tasksLabel = new JBLabel("<html><h2>Tasks</h2></html>");
      topPanel.add(tasksLabel);
      JBPanel tasks = new JBPanel();
      tasks.setBorder(new EmptyBorder(0, 10, 0, 0));
      tasks.setLayout(new BoxLayout(tasks, BoxLayout.Y_AXIS));
      topPanel.add(tasks);
      //loop through every map in list
      for (LinkedTreeMap map : list) {
        int projectId = ((Double)map.get("ProjectId")).intValue();
        int artifactId = ((Double)map.get("TaskId")).intValue();
        String description = (String)map.get("Description");
        String projectName = (String)map.get("ProjectName");
        String priorityName = (String)map.get("TaskPriorityName");
        String name = (String)map.get("Name");
        String status = (String)map.get("TaskStatusName");
        String type = (String)map.get("TaskTypeName");


        //create an artifact with the fields from above
        Artifact artifact = new Task(projectId, projectName, artifactId, name, priorityName);
        //set the description
        artifact.setDescription(description);
        artifact.setStatus(status);
        artifact.setType(type);
        JBLabel label = new JBLabel(name);
        //allow the user to click on the label
        label.addMouseListener(new TopLabelMouseListener(artifact, label, this, credentials.getUrl()));
        tasks.add(label);
      }
      //enable expand/collapse features
      tasksLabel.addMouseListener(new TreeListener(tasks, tasksLabel));
    }
  }

  /**
   * Performs a REST call and adds all incidents to {@code topPanel}
   */
  private void addIncidents(SpiraTeamCredentials credentials) throws IOException {
    //create a new Gson object
    Gson gson = new Gson();
    //list which contain all of the information on incidents from the REST request
    ArrayList<LinkedTreeMap> list = SpiraTeamUtil.getAssignedIncidents(credentials);
    //only add incidents if there is at least one returned from the REST request
    if(list.size() > 0) {
      //incidents 'parent' label
      JBLabel incidentsLabel = new JBLabel("<html><h2>Incidents</h2></html>");
      //add the title to the panel
      topPanel.add(incidentsLabel);
      //create a panel which will fit under the big incidentsLabel
      JBPanel incidents = new JBPanel();
      //make the panel have no border
      incidents.setBorder(new EmptyBorder(0, 10, 0, 0));
      //make the panel lay out its children horizontally
      incidents.setLayout(new BoxLayout(incidents, BoxLayout.Y_AXIS));
      //add the incidents panel to the main top panel
      topPanel.add(incidents);
      //for each LinkedTreeMap in list
      for (LinkedTreeMap map : list) {
        //get the project Id, cast it to a double and get its integer value
        int projectId = ((Double)map.get("ProjectId")).intValue();
        //get the incident Id, cast it to a double and get its integer value
        //we call it artifact Id as it is a property in the Artifact class
        int artifactId = ((Double)map.get("IncidentId")).intValue();
        //get the description of the artifact
        String description = (String)map.get("Description");
        //get the project name of the artifact
        String projectName = (String)map.get("ProjectName");
        String priorityName = (String)map.get("PriorityName");
        //the name of the artifact
        String name = (String)map.get("Name");
        String status = (String)map.get("IncidentStatusName");
        //the type of incident ex bug, incident, etc
        String type = (String)map.get("IncidentTypeName");


        //create an artifact with the fields from above
        Artifact artifact = new Incident(projectId, projectName, artifactId, name, priorityName);
        //set the description
        artifact.setDescription(description);
        artifact.setStatus(status);
        artifact.setType(type);
        //create a label which says the name of the artifact
        JBLabel label = new JBLabel(name);
        //add a listener, see the LabelMouseListener class below
        label.addMouseListener(new TopLabelMouseListener(artifact, label, this, credentials.getUrl()));
        //add the label to the incidents panel
        incidents.add(label);
      }
      //add a TreeListener (see below) to the label, passing in the panel
      //this listener shows the incidents panel when the incidents label is pressed
      incidentsLabel.addMouseListener(new TreeListener(incidents, incidentsLabel));
    }
  }

  /**
   * Show information in the bottom panel about the provided artifact
   * @param artifact The artifact to show information about
   * @param baseURL The base URL of the user
   */
  public void showInformation(Artifact artifact, String baseURL) {
    //remove everything currently stored in the bottomPanel
    bottomPanel.removeAll();
    //show the name of the artifact as the title of the bottom panel
    JBLabel title = new JBLabel("<html><div><h2>" + artifact.getPrefix() + ":" + artifact.getArtifactId()
                                + " - " + artifact.getName() + "</h2></div></html>");
    //allow user to click title to take to SpiraTeam
    title.addMouseListener(new HyperlinkListener(SpiraTeamUtil.getArtifactURI(artifact, baseURL), title));
    bottomPanel.add(title);
    //label which will contain a table of all the values, removes the border
    JBLabel table = new JBLabel("<html><style>th {padding-right: 20px; text-align: left;}</style><table border=\"0\">");
    String type = artifact.getType();
    //only show type if it is not null
    if(type != null) {
      String text = table.getText();
      text+="<tr>";
      text+="<th>Type</th>";
      text+="<td>" + type + "</td>";
      text+="</tr>";
      table.setText(text);
    }
    String project = artifact.getProjectName();
    if(project != null) {
      String text = table.getText();
      text+="<tr>";
      text+="<th>Project</th>";
      text+="<td>" + project + "</td>";
      text+="</tr>";
      table.setText(text);
    }
    String status = artifact.getStatus();
    if(status != null) {
      String text = table.getText();
      text+="<tr>";
      text+="<th>Status</th>";
      text+="<td>" + status + "</td>";
      text+="</tr>";
      table.setText(text);
    }
    table.setText(table.getText() + "</table></html>");
    bottomPanel.add(table);

    String description = artifact.getDescription();
    if(description != null) {
      JBLabel descriptionLabel = new JBLabel("<html><strong>Description:</strong><br><div style=\"word-wrap: normal\">" + description + "</div></html>");
      bottomPanel.add(descriptionLabel);
    }


    //need to show the changes
    bottomPanel.updateUI();
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow window) {
    SpiraTeamCredentials credentials = SpiraTeamCredentials.loadCredentials();
    //SpiraTeamCredentials credentials = ServiceManager.getService(SpiraTeamCredentials.class);
    System.out.println(credentials);
    try {
      //add requirements to the top panel
      addRequirements(credentials);
      //add tasks to the top panel
      addTasks(credentials);
      //add incidents to the top panel
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
  //ignore the three methods below, they are not used
  @Override
  public void init(ToolWindow window) {
    //not used
  }
  @Override
  public boolean shouldBeAvailable(@NotNull Project project) {
    //not used
    return false;
  }
  @Override
  public boolean isDoNotActivateOnStart() {
    //not used
    return false;
  }
}

/**
 * Adds functionality for creating custom trees
 * <p>When the user clicks the given label, the panel passed in with the artifact names is expanded</p>
 */
class TreeListener implements MouseListener {
  private static String expandButton = "▶ ";
  private static String collapseButton = "▼ ";
  JBPanel panel;
  JBLabel label;
  boolean isExpanded = false;

  public TreeListener(JBPanel panel, JBLabel label) {
    this.panel = panel;
    this.label = label;
    //add the expand button
    String text = label.getText();
    int startLoc = text.indexOf("<h2>") + 4;
    text = text.substring(0, startLoc) + "<span style=\"font-size: .6em\">" + expandButton + "</span>" + text.substring(startLoc);
    label.setText(text);
    //make panel invisible by default
    panel.setVisible(false);
  }

  /**
   * The only method we care about, the others are irrelevant
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    //hide the list if it is already expanded
    if (isExpanded) {
      //hide the artifacts
      panel.setVisible(false);
      isExpanded = false;
      //turn the collapse button into an expand button
      String text = label.getText();
      int startLoc = text.indexOf(collapseButton);
      text = text.substring(0, startLoc) + expandButton + text.substring(startLoc + collapseButton.length());
      label.setText(text);
    }
    //show the list if it is not expanded
    else {
      //show the artifacts
      panel.setVisible(true);
      isExpanded = true;
      //turn the expand button into a collapse button
      String text = label.getText();
      int startLoc = text.indexOf(expandButton);
      text = text.substring(0, startLoc) + collapseButton + text.substring(startLoc + expandButton.length());
      label.setText(text);
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
    //do nothing
  }
}

/**
 * Allows users to click on labels and underlines the label when the user hovers over it
 */
class TopLabelMouseListener implements MouseListener {
  private Artifact artifact;
  private JBLabel label;
  /**
   * Used only to show information in the bottom panel when a label is clicked
   */
  private SpiraToolWindowFactory window;
  private String baseURL;

  public TopLabelMouseListener(Artifact artifact, JBLabel label, SpiraToolWindowFactory window, String baseURL) {
    this.artifact = artifact;
    this.label = label;
    this.window = window;
    this.baseURL = baseURL;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    //show additional information on the artifact in the bottom panel
    window.showInformation(artifact, baseURL);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    //do nothing
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    //do nothing
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    Font font = label.getFont();
    //create a Map with the attributes of the font
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
   * @deprecated Popups are no longer used by the SpiraTeam Plugin
   */
  private JBPanel createPanel() {
    JBPanel panel = new JBPanel();
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    //contains the artifact prefix and ID as well as the project
    JBLabel title = new JBLabel(artifact.getPrefix() + ":" + artifact.getArtifactId() + "   Project: " + artifact.getProjectName());
    panel.add(title);
    //contains the description, wrapped in html as Description supports rich text
    panel.add(new JBLabel("<html>Description: " + artifact.getDescription() + "</html>"));
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

/**
 * Used to open the given hyperlink when clicked
 */
class HyperlinkListener implements MouseListener {
  URI uri;
  JBLabel label;
  public HyperlinkListener(URI uri, JBLabel label) {
    this.uri = uri;
    this.label = label;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    SpiraTeamUtil.openURL(uri);
  }

  @Override
  public void mousePressed(MouseEvent e) {

  }

  @Override
  public void mouseReleased(MouseEvent e) {

  }

  @Override
  public void mouseEntered(MouseEvent e) {
    String oldText = label.getText();
    int startLoc = oldText.indexOf("<h2>");
    int endLoc = oldText.indexOf("</h2>");
    //add in the underline tag between the h2 tags
    String newText = oldText.substring(0, startLoc + 4) + "<u>" +
                     oldText.substring(startLoc+4, endLoc) + "</u>" + oldText.substring(endLoc);
    label.setText(newText);
    label.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  @Override
  public void mouseExited(MouseEvent e) {
    String oldText = label.getText();
    int startLoc = oldText.indexOf("<u>");
    int endLoc = oldText.indexOf("</u>");
    //remove the underline tags
    String newText = oldText.substring(0, startLoc) + oldText.substring(startLoc+3, endLoc) + oldText.substring(endLoc+4);
    label.setText(newText);
    label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }
}
