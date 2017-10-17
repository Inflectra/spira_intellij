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
import com.inflectra.idea.core.listeners.*;
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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
   * Click on button to refresh the window
   */
  private JButton refresh;
  /**
   * Panel with information on the signed in user and a refresh button
   */
  private JBPanel topInformationPanel;
  //panel for each requirement type
  private JBPanel requirements;
  private JBPanel tasks;
  private JBPanel incidents;

  //labels which form the 'tree'
  private JBLabel requirementsLabel;
  private JBLabel incidentsLabel;
  private JBLabel tasksLabel;
  /**
   * Contains the last time the window was refreshed
   */
  private JBLabel dateRefreshed;
  /**
   * The current date and time
   */
  private Date date;
  /**
   * The format to display the date in
   */
  private DateFormat dateFormat;

  private JBPanel invalidInformationPanel;
  /**
   * The current instance of SpiraToolWindowFactory
   */
  public static SpiraToolWindowFactory instance;

  public SpiraToolWindowFactory() {
    instance = this;
    topPanel = new JBPanel();
    topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
    //make the panel lay out its children vertically, instead of horizontally
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.setAlignmentX(0);

    bottomPanel = new JBPanel();
    bottomPanel.setBorder(new EmptyBorder(5,10,5,10));
    //make the panel lay out its children vertically, instead of horizontally
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

  }
  /**
   * Reloads the contents of the SpiraTeam Window
   */
  public static void reload(Project project) {
    //remove the panel which informs the user about invalid information
    if(instance.invalidInformationPanel != null)
      instance.invalidInformationPanel.setVisible(false);

    SpiraTeamCredentials credentials = ServiceManager.getService(SpiraTeamCredentials.class);
    //show the username of the authenticated user
    instance.showTopInformation(project, credentials);
    //need to remake the date
    instance.date = new Date();
    instance.dateRefreshed.setText("Last refreshed: " + instance.dateFormat.format(instance.date));
    instance.refresh.setText("Refreshing...");

    //must update the changes
    instance.topInformationPanel.updateUI();
    //clearing the bottom panel, and updating it if the artifact is no longer assigned to the user
    instance.bottomPanel.removeAll();
    //these are required because of how swing works
    instance.bottomPanel.updateUI();
    instance.refresh.updateUI();

    //refresh the options
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          instance.addRequirements(credentials);
          //add tasks to the top panel
          instance.addTasks(credentials);
          //add incidents to the top panel
          instance.addIncidents(credentials);

          instance.refresh.setText("Refresh");
          showNotification("Successfully refreshed!");
        }
        catch (IOException e) {
          instance.showInvalidInformation(project);
        }
      }
    });
  }

  /**
   * Displays a notification in the top panel below the logged-in user
   * @param text The text to display in the notification
   */
  public static void showNotification(String text) {
    /*JBLabel titleLabel = new JBLabel("There was a problem. Please refer to the event log inside the SpiraTeam application");
    Font font = titleLabel.getFont();
    //set the font size
    font = font.deriveFont((float)20);
    titleLabel.setFont(font);
    panel.add(titleLabel);*/

    JBLabel topErrorMessage = new JBLabel(text);
    //add a listener to disappear on click
    topErrorMessage.addMouseListener(new DisappearListener(instance.topInformationPanel, topErrorMessage));
    //create empty space between them
    instance.topInformationPanel.add(topErrorMessage);
    instance.topInformationPanel.updateUI();

    //have the UI update
    instance.bottomPanel.updateUI();
  }


  /**
   * Shows the screen which informs the user that their credentials are invalid
   */
  private void showInvalidInformation(Project project) {
    //clear both panels
    topPanel.removeAll();
    bottomPanel.removeAll();
    //reset all panels and buttons
    refresh = null;
    requirements = null;
    tasks = null;
    incidents = null;
    topInformationPanel = null;

    invalidInformationPanel = new JBPanel();
    invalidInformationPanel.setBorder(new EmptyBorder(0,0,0,0));
    invalidInformationPanel.setLayout(new BoxLayout(invalidInformationPanel, BoxLayout.Y_AXIS));

    invalidInformationPanel.add(new JBLabel("<html><h2>Not what you were looking for?</h2></html>"));
    invalidInformationPanel.add(new JBLabel("Your authentication credentials may be wrong."));
    invalidInformationPanel.add(new JBLabel("Please verify them by clicking the button below"));
    invalidInformationPanel.add(new JBLabel("And check your URL (e.g. that it starts with http:// or https://)"));
    JButton button = new JButton("View Credentials");
    button.addActionListener(l -> {
      SpiraTeamLoginDialog dialog = new SpiraTeamLoginDialog(project, ServiceManager.getService(SpiraTeamCredentials.class));
      dialog.show();
    });
    invalidInformationPanel.add(button);
    //update the changes
    invalidInformationPanel.updateUI();
    topPanel.add(invalidInformationPanel);
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
    //create the panel if it does not exist
    if(topInformationPanel == null) {
      //panel which will contain information and various buttons
      topInformationPanel = new JBPanel();
      topInformationPanel.setAlignmentX(0);
      //have the panel lay out its children horizontally
      topInformationPanel.setLayout(new BoxLayout(topInformationPanel, BoxLayout.Y_AXIS));
      topInformationPanel.setBorder(new EmptyBorder(0,0,0,0));
      topInformationPanel.setAlignmentX(0);
      topInformationPanel.setAlignmentY(0);

      //panel which contains the buttons at the top
      JBPanel buttonPanel = new JBPanel();
      buttonPanel.setAlignmentX(0);
      buttonPanel.setAlignmentY(0);
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      buttonPanel.setBorder(new EmptyBorder(0,0,0,0));
      //click on button to refresh from server
      refresh = new JButton("Refresh");
      refresh.setAlignmentX(0);
      //on button clicked
      refresh.addActionListener(l -> {
        //reload the tool window
        reload(project);
      });
      buttonPanel.add(refresh);
      JButton home = new JButton("Home");
      home.setAlignmentX(0);
      //open My Page in browser when clicked
      home.addActionListener(l -> {
        //create the MyPage URL
        URI myPage = SpiraTeamUtil.getMyPageURL(credentials);
        //open the URL
        SpiraTeamUtil.openURL(myPage);
      });
      //add the button to the panel
      buttonPanel.add(home);

      JButton newArtifact = new JButton("New");
      newArtifact.setAlignmentX(0);
      newArtifact.addActionListener(l -> {
        SpiraTeamNewArtifactDialog artifact = new SpiraTeamNewArtifactDialog(project, credentials);
        artifact.show();
      });
      buttonPanel.add(newArtifact);
      topInformationPanel.add(buttonPanel);

      //panel which contains the signed-in user
      JBPanel signedIn = new JBPanel();
      signedIn.setBorder(new EmptyBorder(0,0,0,0));
      signedIn.setLayout(new BoxLayout(signedIn, BoxLayout.X_AXIS));
      signedIn.setAlignmentX(0);
      signedIn.setAlignmentY(0);
      //show who is signed in
      JBLabel signedInText = new JBLabel("Signed in as: ");
      signedInText.setAlignmentX(0);
      signedIn.add(signedInText);
      //clickable label
      JBLabel signedInUser = new JBLabel(credentials.getUsername());
      signedInUser.setAlignmentX(0);
      signedInUser.addMouseListener(new UsernameListener(credentials, project, signedInUser));
      signedIn.add(signedInUser);

      date = new Date();
      //the format to provide the date in
      dateFormat = new SimpleDateFormat("MM/dd HH:mm");

      dateRefreshed = new JBLabel("Last refreshed: " + dateFormat.format(date));
      //make the color more transparent
      dateRefreshed.setForeground(UIUtil.getHeaderInactiveColor());
      //add spacing
      signedIn.add(Box.createRigidArea(new Dimension(10,0)));
      signedIn.add(dateRefreshed);

      topInformationPanel.add(signedIn);

      //add the information panel to the top panel
      topPanel.add(topInformationPanel);
    }
  }

  /**
   * Builds the SpiraTeam Window content when the application is launched
   */
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow window) {
    //get the credentials from the IDE
    SpiraTeamCredentials credentials = ServiceManager.getService(SpiraTeamCredentials.class);
    //store the tool window for future use
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
    if(requirements == null) {
      //create a panel which will fit under the big requirementsLabel
      requirements = new JBPanel();
      requirements.setBorder(new EmptyBorder(0, 10, 0, 0));
      requirements.setLayout(new BoxLayout(requirements, BoxLayout.Y_AXIS));
      requirements.setVisible(false);
      requirements.setAlignmentX(0);

      //requirements 'parent' label
      requirementsLabel = new JBLabel("<html><h2>Requirements</h2></html>");
      requirementsLabel.setAlignmentX(0);
      //add a TreeListener to the label, passing in the panel
      //this listener shows the requirements panel when the requirements label is pressed
      requirementsLabel.addMouseListener(new TreeListener(requirements, requirementsLabel));
      //add the title to the panel
      topPanel.add(requirementsLabel);
      topPanel.add(requirements);
    }
    else {
      //clear the panel, to repopulate it
      requirements.removeAll();
    }
    Gson gson = new Gson();
    //getAssignedRequirements returns an InputStream with the JSON from the REST request, that is then read by the JsonReader
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedRequirements(credentials)));
    //Turn the JSON into something java understands
    List<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    //only show requirements if there are any assigned to the user
    if(list.size() > 0) {
      //show the label
      requirementsLabel.setVisible(true);
      //loop through every LinkedTreeMap in list
      for (LinkedTreeMap map : list) {
        addArtifactToPanel(map, credentials);
      }
    }
    else {
      //need to hide the label
      requirementsLabel.setVisible(false);
    }
  }

  /**
   * Performs a REST call and adds all tasks to {@code topPanel}
   */
  private void addTasks(SpiraTeamCredentials credentials) throws IOException {
    if(tasks == null) {
      //create a panel which will fit under the big tasksLabel
      tasks = new JBPanel();
      tasks.setBorder(new EmptyBorder(0, 10, 0, 0));
      tasks.setLayout(new BoxLayout(tasks, BoxLayout.Y_AXIS));
      tasks.setVisible(false);
      tasks.setAlignmentX(0);

      //tasks 'parent' label
      tasksLabel = new JBLabel("<html><h2>Tasks</h2></html>");
      tasksLabel.setAlignmentX(0);
      //add a TreeListener to the label, passing in the panel
      //this listener shows the tasks panel when the tasks label is pressed
      tasksLabel.addMouseListener(new TreeListener(tasks, tasksLabel));
      //add the title to the panel
      topPanel.add(tasksLabel);
      topPanel.add(tasks);
    }
    else {
      //clear the panel, to repopulate it
      tasks.removeAll();
    }
    Gson gson = new Gson();
    //getAssignedRequirements returns an InputStream with the JSON from the REST request, that is then read by the JsonReader
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedTasks(credentials)));
    //Turn the JSON into something java understands
    ArrayList<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    //only add if there are assigned tasks
    if(list.size() > 0) {
      tasksLabel.setVisible(true);
      //loop through every map in list
      for (LinkedTreeMap map : list) {
        addArtifactToPanel(map, credentials);
      }
    }
    else {
      //need to hide the label
      tasksLabel.setVisible(false);
    }
  }

  /**
   * Performs a REST call and adds all incidents to {@code topPanel}
   */
  private void addIncidents(SpiraTeamCredentials credentials) throws IOException {
    if(incidents == null) {
      //create a panel which will fit under the big incidentsLabel
      incidents = new JBPanel();
      //make the panel have no border
      incidents.setBorder(new EmptyBorder(0, 10, 0, 0));
      //make the panel lay out its children horizontally
      incidents.setLayout(new BoxLayout(incidents, BoxLayout.Y_AXIS));
      incidents.setVisible(false);
      incidents.setAlignmentX(0);

      //incidents 'parent' label
      incidentsLabel = new JBLabel("<html><h2>Incidents</h2></html>");
      incidentsLabel.setAlignmentX(0);
      //add a TreeListener to the label, passing in the panel
      //this listener shows the incidents panel when the incidents label is pressed
      incidentsLabel.addMouseListener(new TreeListener(incidents, incidentsLabel));
      //add the title to the panel
      topPanel.add(incidentsLabel);
      topPanel.add(incidents);
    }
    else {
      //clear the panel
      incidents.removeAll();
    }
    Gson gson = new Gson();
    //getAssignedIncidents returns an InputStream with the JSON from the REST request, that is then read by the JsonReader
    JsonReader jsonReader = new JsonReader(new InputStreamReader(SpiraTeamUtil.getAssignedIncidents(credentials)));
    //Turn the JSON into something java understands
    List<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    //only show incidents if there are any assigned to the user
    if(list.size() > 0) {
      //show the label
      incidentsLabel.setVisible(true);
      //loop through every LinkedTreeMap in list
      for (LinkedTreeMap map : list) {
        addArtifactToPanel(map, credentials);
      }
    }
    else {
      //need to hide the label
      incidentsLabel.setVisible(false);
    }
  }

  /**
   * Adds the information to the correct panel. Checks which panel to add to dynamically
   * @param map Map which contains the information needed to add an artifact
   */
  public void addArtifactToPanel(LinkedTreeMap map, SpiraTeamCredentials credentials) {

    //get common properties
    //get the project name of the artifact
    String projectName = (String)map.get("ProjectName");
    //the name of the artifact
    String name = (String)map.get("Name");
    //get the description of the artifact
    String description = (String)map.get("Description");
    //get the project Id, cast it to a double and get its integer value
    int projectId = ((Double)map.get("ProjectId")).intValue();

    //if the artifact is a requirement
    if(map.containsKey("RequirementTypeName")) {
      int artifactId = ((Double)map.get("RequirementId")).intValue();
      String priorityName = (String)map.get("ImportanceName");
      //workflow status name
      String status = (String)map.get("StatusName");
      String type = (String)map.get("RequirementTypeName");

      //create an artifact with the fields from above
      Artifact artifact = new Requirement(projectId, projectName, artifactId, name, priorityName);
      //set the description of the artifact
      artifact.setDescription(description);
      artifact.setStatus(status);
      JBLabel label = new JBLabel(name);
      label.setAlignmentX(0);
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
    //if the artifact is an incident
    else if(map.containsKey("IncidentTypeName")) {
      //get the incident Id, cast it to a double and get its integer value
      //we call it artifact Id as it is a property in the Artifact class
      int artifactId = ((Double)map.get("IncidentId")).intValue();
      String priorityName = (String)map.get("PriorityName");
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
      label.setAlignmentX(0);
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
    //if the artifact is a task
    else if(map.containsKey("TaskPriorityName")) {
      int artifactId = ((Double)map.get("TaskId")).intValue();
      String priorityName = (String)map.get("TaskPriorityName");
      String status = (String)map.get("TaskStatusName");
      String type = (String)map.get("TaskTypeName");

      //create an artifact with the fields from above
      Artifact artifact = new Task(projectId, projectName, artifactId, name, priorityName);
      //set the description
      artifact.setDescription(description);
      artifact.setStatus(status);
      artifact.setType(type);
      JBLabel label = new JBLabel(name);
      label.setAlignmentX(0);
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
    topPanel.updateUI();
  }
}
