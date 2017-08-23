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
import com.inflectra.idea.core.listeners.HyperlinkListener;
import com.inflectra.idea.core.listeners.TopLabelMouseListener;
import com.inflectra.idea.core.listeners.TreeListener;
import com.inflectra.idea.core.listeners.UsernameListener;
import com.inflectra.idea.core.model.artifacts.*;
import com.inflectra.idea.ui.dialogs.SpiraTeamLoginDialog;
import com.inflectra.idea.ui.dialogs.SpiraTeamNewArtifactDialog;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Is the 'core' of the plug-in's UI, this is the class from which the SpiraToolWindow originates from
 * @author Peter Geertsema
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
  /**
   * The artifact label that has been selected in the top
   */
  private JBLabel selectedLabel;
  /**
   * The tool window which contains the information
   */
  private ToolWindow window;
  /**
   * The current instance of SpiraToolWindowFactory
   */
  private static SpiraToolWindowFactory instance;

  private JBLabel refreshLabel;

  public SpiraToolWindowFactory() {
    instance = this;
    topPanel = new JBPanel();
    topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
    //make the panel lay out its children vertically, instead of horizontally
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

    bottomPanel = new JBPanel();
    bottomPanel.setBorder(new EmptyBorder(5,10,5,10));
    //make the panel lay out its children vertically, instead of horizontally
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

    refreshLabel = new JBLabel("Refreshed!");
    //hide by default
    refreshLabel.setVisible(false);
    //create a new font with a size of 20
    Font newFont = instance.refreshLabel.getFont().deriveFont((float)20);
    instance.refreshLabel.setFont(newFont);
  }
  /**
   * Reloads the contents of the SpiraTeam Window
   */
  public static void reload(Project project) {
    SpiraTeamCredentials credentials = ServiceManager.getService(SpiraTeamCredentials.class);
    try {
      //clear the top panel
      instance.topPanel.removeAll();

      instance.showTopInformation(project, credentials);
      //show the username of the authenticated user
      instance.addRequirements(credentials);
      //add tasks to the top panel
      instance.addTasks(credentials);
      //add incidents to the top panel
      instance.addIncidents(credentials);
      //make the label visible at the top
      instance.refreshLabel.setVisible(true);
    }
    catch(IOException e) {
      instance.showInvalidInformation(project);
    }
  }

  /**
   * Hides the refresh label
   */
  public static void hideRefreshLabel() {
    instance.refreshLabel.setVisible(false);
  }

  /**
   * Shows the screen which informs the user that their credentials are invalid
   */
  private void showInvalidInformation(Project project) {
    //clear both panels
    topPanel.removeAll();
    bottomPanel.removeAll();

    topPanel.add(new JBLabel("<html><h2>Not what you were looking for?</h2></html>"));
    topPanel.add(new JBLabel("Your authentication credentials may be wrong."));
    topPanel.add(new JBLabel("Please verify them by clicking the button below"));
    JButton button = new JButton("View Credentials");
    button.addActionListener(l -> {
      SpiraTeamLoginDialog dialog = new SpiraTeamLoginDialog(project, ServiceManager.getService(SpiraTeamCredentials.class));
      dialog.show();
    });
    topPanel.add(button);
    //update the changes
    topPanel.updateUI();
    bottomPanel.updateUI();
  }

  /**
   * Show information in the bottom panel about the provided artifact
   * @param artifact The artifact to show information about
   * @param credentials The log-in credentials of the user
   * @param label The label that was just selected
   */
  public void showInformation(Artifact artifact, SpiraTeamCredentials credentials, JBLabel label) {
    //only run if there was a previously selected artifact
    if(selectedLabel != null) {
      //change the color back to normal
      Color color = UIUtil.getActiveTextColor();
      selectedLabel.setForeground(color);
    }
    //have the plug-in remember the last opened artifact
    credentials.setLastOpenArtifactId(artifact.getArtifactId());
    credentials.setLastOpenArtifactType(artifact.getArtifactType());

    //set the currently selected label to be the new one and change the color
    selectedLabel = label;
    Color color = UIUtil.getListSelectionBackground();
    selectedLabel.setForeground(color);

    //remove everything currently stored in the bottomPanel
    bottomPanel.removeAll();
    //show the name of the artifact as the title of the bottom panel
    JBLabel title = new JBLabel("<html><div><h2>" + artifact.getPrefix() + ":" + artifact.getArtifactId()
                                + " - " + artifact.getName() + "</h2></div></html>");
    //allow user to click title to take to SpiraTeam
    title.addMouseListener(new HyperlinkListener(SpiraTeamUtil.getArtifactURI(artifact, credentials.getUrl()), title));
    bottomPanel.add(title);
    //label which will contain a table of all the values. Has no border
    JBLabel table = new JBLabel("<html><style>th {padding-right: 20px; text-align: left;}</style><table border=\"0\">");

    String type = artifact.getType();
    //only show type if it is not null
    if(type != null) {
      addContentToTable(table, "Type", type);
    }
    String project = artifact.getProjectName();
    if(project != null) {
      addContentToTable(table, "Project", project);
    }
    String status = artifact.getStatus();
    if(status != null) {
      addContentToTable(table, "Status", status);
    }
    String priority = artifact.getPriorityName();
    if(priority != null) {
      addContentToTable(table, "Priority", priority);
    }
    //end the table
    table.setText(table.getText() + "</table></html>");
    bottomPanel.add(table);
    //show description separately
    String description = artifact.getDescription();
    if(description != null) {
      JBLabel descriptionLabel = new JBLabel("<html><strong>Description:</strong><br><div style=\"word-wrap: normal\">" + description + "</div></html>");
      bottomPanel.add(descriptionLabel);
    }

    //need to show the changes
    bottomPanel.updateUI();
  }

  /**
   * Utility method used to add the given header and data to the table contained in the label
   * @param table The table to add the content to
   * @param header The name of the property to be shown
   * @param data The data associated with the header
   */
  private static void addContentToTable(JBLabel table, String header, String data) {
    String text = table.getText();
    text+="<tr>";
    text+="<th>" + header + "</th>";
    text+="<td>" + data + "</td>";
    text+="</tr>";
    table.setText(text);
  }

  /**
   * Adds information to the top such as the currently signed in user as well as a refresh button
   * @param project
   * @param credentials
   */
  private void showTopInformation(Project project, SpiraTeamCredentials credentials) {
    //panel which will contain information and various buttons
    JBPanel panel = new JBPanel();
    panel.setAlignmentX(0);
    //have the panel lay out its children horizontally
    LayoutManager layout = new BoxLayout(panel, BoxLayout.X_AXIS);
    panel.setLayout(layout);
    panel.setBorder(new EmptyBorder(0,0,0,0));

    //show who is signed in
    JBLabel signedInText = new JBLabel("Signed in as: ");
    panel.add(signedInText);
    //clickable label
    JBLabel signedInUser = new JBLabel(credentials.getUsername());
    signedInUser.addMouseListener(new UsernameListener(credentials, project, signedInUser));
    panel.add(signedInUser);

    //click on button to refresh from server
    JButton refresh = new JButton("Refresh");
    //on button clicked
    refresh.addActionListener(l -> {
      //reload the tool window
      reload(project);
    });
    panel.add(refresh);
    JButton home = new JButton("Home");
    //open My Page in browser when clicked
    home.addActionListener(l -> {
      //create the MyPage URL
      URI myPage = SpiraTeamUtil.getMyPageURL(credentials);
      //open the URL
      SpiraTeamUtil.openURL(myPage);
    });
    //add the button to the panel
    panel.add(home);

    JButton newArtifact = new JButton("New");
    newArtifact.addActionListener(l -> {
      SpiraTeamNewArtifactDialog artifact = new SpiraTeamNewArtifactDialog(project, credentials);
      artifact.show();
    });
    panel.add(newArtifact);

    //add the now populated panel to the top
    topPanel.add(panel);
  }

  /**
   * Builds the SpiraTeam Window content when the application is launched
   */
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow window) {
    //get the credentials from the IDE
    SpiraTeamCredentials credentials = ServiceManager.getService(SpiraTeamCredentials.class);
    //store the tool window for future use
    this.window = window;
    try {
      if(credentials != null) {
        showTopInformation(project, credentials);
        //add requirements to the top panel
        addRequirements(credentials);
        //add tasks to the top panel
        addTasks(credentials);
        //add incidents to the top panel
        addIncidents(credentials);
      }
      else
        showInvalidInformation(project);
    }
    catch (Exception e) {
      //prompt the user to re-enter authentication information
      showInvalidInformation(project);
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

  /**
   * Performs a REST call and adds all requirements to {@code topPanel}
   */
  private void addRequirements(SpiraTeamCredentials credentials) throws IOException {
    Gson gson = new Gson();
    //getAssignedRequirements returns an InputStream with the JSON from the REST request, that is then read by the JsonReader
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedRequirements(credentials)));
    //Turn the JSON into something java understands
    List<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
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
        label.addMouseListener(new TopLabelMouseListener(artifact, label, this, credentials));
        requirements.add(label);
        //create empty space between the artifacts
        requirements.add(Box.createRigidArea(new Dimension(0,3)));
        //if the last opened artifact is the same as the one currently being built, show it in the bottom panel
        //this allows artifacts to stay open through restarts and refreshes
        if(credentials.getLastOpenArtifactType() == ArtifactType.REQUIREMENT && credentials.getLastOpenArtifactId() == artifactId) {
          showInformation(artifact, credentials, label);
        }
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
        label.addMouseListener(new TopLabelMouseListener(artifact, label, this, credentials));
        tasks.add(label);
        //create empty space between the artifacts
        tasks.add(Box.createRigidArea(new Dimension(0,3)));
        //if the last opened artifact is the same as the one currently being built, show it in the bottom panel
        //this allows artifacts to stay open through restarts and refreshes
        if(credentials.getLastOpenArtifactType() == ArtifactType.TASK && credentials.getLastOpenArtifactId() == artifactId) {
          showInformation(artifact, credentials, label);
        }
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
    List<LinkedTreeMap> list = SpiraTeamUtil.getAssignedIncidents(credentials);
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
        label.addMouseListener(new TopLabelMouseListener(artifact, label, this, credentials));
        //add the label to the incidents panel
        incidents.add(label);
        //create empty space between the artifacts
        incidents.add(Box.createRigidArea(new Dimension(0,3)));
        //if the last opened artifact is the same as the one currently being built, show it in the bottom panel
        //this allows artifacts to stay open through restarts and refreshes
        if(credentials.getLastOpenArtifactType() == ArtifactType.INCIDENT && credentials.getLastOpenArtifactId() == artifactId) {
          showInformation(artifact, credentials, label);
        }
      }
      //add a TreeListener (see below) to the label, passing in the panel
      //this listener shows the incidents panel when the incidents label is pressed
      incidentsLabel.addMouseListener(new TreeListener(incidents, incidentsLabel));
    }
  }
}
