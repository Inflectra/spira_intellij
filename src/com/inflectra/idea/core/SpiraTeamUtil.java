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
package com.inflectra.idea.core;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import com.inflectra.idea.core.model.SpiraTeamPriority;
import com.inflectra.idea.core.model.SpiraTeamUser;
import com.inflectra.idea.core.model.artifacts.Artifact;
import com.inflectra.idea.core.model.artifacts.ArtifactType;
import com.inflectra.idea.core.model.SpiraTeamArtifactType;
import com.inflectra.idea.core.model.SpiraTeamProject;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with a wide variety of utility methods used throughout the plug-in
 *
 * @author Peter Geertsema
 */
public class SpiraTeamUtil {
  /**
   * The URL appended to the base URL to access REST. Note that it ends with a slash
   */
  private static String restServiceUrl = "/services/v5_0/RestService.svc/";

  /**
   * @param artifactTypeId The ID of the artifact type
   * @return the artifact type corresponding to the ID, null if the artifact type id is not supported
   */
  public static ArtifactType getArtifactType(int artifactTypeId) {
    switch (artifactTypeId) {
      case 1:
        return ArtifactType.REQUIREMENT;
      case 3:
        return ArtifactType.INCIDENT;
      case 6:
        return ArtifactType.TASK;
      default:
        return null;
    }
  }

  /**
   * Opens the URI, if possible
   *
   * @param in The URI
   */
  public static void openURL(URI in) {
    //only attempt to open URI if it is supported
    if (Desktop.isDesktopSupported()) {
      try {
        //open the URI in browser
        Desktop.getDesktop().browse(in);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      //do nothing
    }
  }

  public static URI getMyPageURL(SpiraTeamCredentials credentials) {
    try {
      //need to have a project ID in the URL for it to work
      List<SpiraTeamProject> availableProjects = getAvailableProjects(credentials);
      if (availableProjects.size() > 0) {
        return new URI(credentials.getUrl() + "/" + availableProjects.get(0).getProjectId() + "/MyPage.aspx");
      }
    }
    catch(Exception e) {
      e.printStackTrace();
      //should never happen as long as credentials are correct
    }
    return null;
  }

  /**
   * @param artifact The artifact we are dealing with
   * @param baseURL  The base URL of the user
   * @return the URI which leads to the SpiraTeam website on that artifact
   */
  public static URI getArtifactURI(Artifact artifact, String baseURL) {
    try {
      //procedurally generates the URI for the artifact in question
      return new URI(baseURL + "/" + artifact.getProjectId() + "/" + artifact.getArtifactType().getArtifactName() +
                     "/" + artifact.getArtifactId() + ".aspx");
    }
    catch (Exception e) {
      //should never happen
      e.printStackTrace();
    }
    return null;
  }

  /**
   * @param credentials The information needed to perform the HTTP request
   * @return An InputStream containing JSON with all requirements assigned to the user
   * @throws IOException If the URL is invalid
   */
  public static InputStream getAssignedRequirements(SpiraTeamCredentials credentials) throws IOException {
    //create the URL
    String url = credentials.getUrl() + restServiceUrl + "requirements?username=" + credentials.getUsername() +
                 "&api-key=" + credentials.getToken();
    //perform the GET request
    return httpGet(url);
  }

  /**
   * @param credentials The information needed to perform the HTTP request
   * @return An InputStream containing JSON with all the tasks assigned to the user
   * @throws IOException If the URL is invalid
   */
  public static InputStream getAssignedTasks(SpiraTeamCredentials credentials) throws IOException {
    //create the URL
    String url = credentials.getUrl() + restServiceUrl + "tasks?username=" + credentials.getUsername() +
                 "&api-key=" + credentials.getToken();
    //perform the GET request
    return httpGet(url);
  }

  /**
   * @param credentials The information needed to perform the HTTP request
   * @return A list of all the Incidents, each Map represents an incident
   * @throws IOException If the URL is invalid
   */
  public static List<LinkedTreeMap> getAssignedIncidents(SpiraTeamCredentials credentials) throws IOException {
    //TODO: Update to use the /incidents REST request when Version 5.3 is released
    //the body of the request
    String body = "[{\"PropertyName\": \"OwnerId\", \"IntValue\": 1}, {\"PropertyName\": \"IncidentStatusId\", \"IntValue\": -2}]";
    //the list to be returned
    ArrayList<LinkedTreeMap> out = new ArrayList<>();
    //get all of the projects available to the user
    List<SpiraTeamProject> projects = getAvailableProjects(credentials);
    //loop through all of the available projects
    for (SpiraTeamProject project : projects) {
      //build the URL for each project
      String url = credentials.getUrl() + restServiceUrl +
                   "projects/" + project.getProjectId() + "/incidents/search" +
                   "?start_row=1&number_rows=1000&sort_by=Priority&username=" + credentials.getUsername() +
                   "&api-key=" + credentials.getToken();
      //add ability to read from the InputStream
      BufferedReader stream = new BufferedReader(new InputStreamReader(httpPost(url, body)));
      Gson gson = new Gson();
      //reading JSON from each project
      ArrayList<LinkedTreeMap> list = gson.fromJson(new JsonReader(stream), ArrayList.class);
      for (LinkedTreeMap map : list) {
        //add each map to the final output list
        out.add(map);
      }
    }
    return out;
  }

  /**
   * @param credentials The credentials
   * @return A list of all the projectIds available to the current user
   * @throws IOException If the URL is invalid
   */
  public static List<SpiraTeamProject> getAvailableProjects(SpiraTeamCredentials credentials) {
    try {
      //create the URL
      String url = credentials.getUrl() + restServiceUrl + "projects?username="
                   + credentials.getUsername() + "&api-key=" + credentials.getToken();
      //perform an HTTP GET request on the specified URL
      InputStream stream = httpGet(url);
      Gson gson = new Gson();
      //create a JSON reader from the JSON sent from the GET request
      JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(stream)));
      //turn JSON into an ArrayList
      ArrayList<LinkedTreeMap> jsonList = gson.fromJson(reader, ArrayList.class);
      //the list to be returned
      ArrayList<SpiraTeamProject> out = new ArrayList<>();
      //loop through each map in the list
      for (LinkedTreeMap map : jsonList) {
        //retrieve the project ID
        Double projectId = (Double)map.get("ProjectId");
        //retrieve the project Name
        String projectName = (String)map.get("Name");
        //add the project to the output list
        out.add(new SpiraTeamProject(projectName, projectId.intValue()));
      }
      return out;
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns an array of all the active users in the current project
   * @param credentials
   * @param projectId The project to look in
   * @return An array of all the active users in the current project
   */
  public static SpiraTeamUser[] getProjectUsers(SpiraTeamCredentials credentials, int projectId) {
    try {
      String url = credentials.getUrl() + restServiceUrl + "projects/" + projectId +
      "/users?username=" + credentials.getUsername() + "&api-key=" + credentials.getToken();
      //perform an HTTP GET request on the specified URL
      InputStream stream = httpGet(url);
      Gson gson = new Gson();
      //create a JSON reader from the JSON sent from the GET request
      JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(stream)));
      //turn JSON into a List
      List<LinkedTreeMap> jsonList = gson.fromJson(reader, ArrayList.class);
      //the array we will return. It is the same size as the above list
      SpiraTeamUser[] out = new SpiraTeamUser[jsonList.size()];
      for(int i=0; i<jsonList.size(); i++) {
        LinkedTreeMap map = jsonList.get(i);
        //get the properties
        String fullName = (String)map.get("FullName");
        int userId = ((Double)map.get("UserId")).intValue();
        String username = (String)map.get("UserName");
        //create the user
        SpiraTeamUser user = new SpiraTeamUser(fullName, userId, username);
        //add the new user to the array
        out[i] = user;
      }
      return out;
    }
    catch(IOException e) {
      e.printStackTrace();
      //should never happen
    }
    return new SpiraTeamUser[0];
  }

  /**
   * @param credentials
   * @param projectId The project to look in
   * @return An array of the incident priorities in the given project
   */
  public static SpiraTeamPriority[] getProjectIncidentPriorities(SpiraTeamCredentials credentials, int projectId) {
    try {
      String url = credentials.getUrl() + restServiceUrl + "projects/" + projectId + "/incidents/priorities" +
                   "?username=" + credentials.getUsername() + "&api-key=" + credentials.getToken();
      //perform an HTTP GET request on the specified URL
      InputStream stream = httpGet(url);
      Gson gson = new Gson();
      //create a JSON reader from the JSON sent from the GET request
      JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(stream)));
      //turn JSON into a List
      List<LinkedTreeMap> jsonList = gson.fromJson(reader, ArrayList.class);
      //the array we will return. It is the same size as the above list
      SpiraTeamPriority[] out = new SpiraTeamPriority[jsonList.size()];
      for(int i=0; i<jsonList.size(); i++) {
        LinkedTreeMap map = jsonList.get(i);
        //get the properties
        int priorityId = ((Double)map.get("PriorityId")).intValue();
        String priorityName = (String)map.get("Name");
        //create the priority
        SpiraTeamPriority toAdd = new SpiraTeamPriority(priorityId, priorityName);
        //add the priority to the array
        out[i] = toAdd;
      }
      return out;
    }
    catch(IOException e) {
      e.printStackTrace();
      //should never happen
    }
    return new SpiraTeamPriority[0];
  }
  /**
   * @return An array of the priorities for requirements
   */
  public static SpiraTeamPriority[] getRequirementPriorities() {
    //the array to return
    SpiraTeamPriority[] out = new SpiraTeamPriority[4];
    //add the priorities
    out[0] = new SpiraTeamPriority(1, "1 - Critical");
    out[1] = new SpiraTeamPriority(2, "2 - High");
    out[2] = new SpiraTeamPriority(3, "3 - Medium");
    out[3] = new SpiraTeamPriority(4, "4 - Low");
    return out;
  }
  /**
   * @return An array of the priorities for tasks
   */
  public static SpiraTeamPriority[] getTaskPriorities() {
    //the array to return
    SpiraTeamPriority[] out = new SpiraTeamPriority[4];
    //add the priorities
    out[0] = new SpiraTeamPriority(1, "1 - Critical");
    out[1] = new SpiraTeamPriority(2, "2 - High");
    out[2] = new SpiraTeamPriority(3, "3 - Medium");
    out[3] = new SpiraTeamPriority(4, "4 - Low");
    return out;
  }

  /**
   * @return An array with all of the requirement types for the given project
   */
  public static SpiraTeamArtifactType[] getRequirementTypes(SpiraTeamCredentials credentials, int projectId) {
    try {
      String url = credentials.getUrl() + restServiceUrl + "projects/" + projectId +
      "/requirements/types?username=" + credentials.getUsername() + "&api-key=" + credentials.getToken();
      //perform the GET request
      BufferedReader stream = new BufferedReader(new InputStreamReader(httpGet(url)));
      Gson gson = new Gson();
      List<LinkedTreeMap> list = gson.fromJson(stream, ArrayList.class);
      //the list we will turn into an array
      SpiraTeamArtifactType[] out = new SpiraTeamArtifactType[list.size()];
      for(int i=0; i<list.size(); i++) {
        LinkedTreeMap map = list.get(i);
        //get the properties from JSON
        int typeId = ((Double)map.get("RequirementTypeId")).intValue();
        String typeName = (String)map.get("Name");
        //create a new artifact type
        SpiraTeamArtifactType toAdd = new SpiraTeamArtifactType(typeId, typeName);
        //add the artifact type to the array
        out[i] = toAdd;
      }
      return out;
    }
    catch(IOException e) {
      e.printStackTrace();
      //should never happen
    }
    return new SpiraTeamArtifactType[0];
  }

  /**
   * @return An array with all of the task types for the given project
   */
  public static SpiraTeamArtifactType[] getTaskTypes(SpiraTeamCredentials credentials, int projectId) {
    try {
      String url = credentials.getUrl() + restServiceUrl + "projects/" + projectId + "/tasks/types" +
                   "?username=" + credentials.getUsername() + "&api-key=" + credentials.getToken();
      //perform the get request
      BufferedReader stream = new BufferedReader(new InputStreamReader(httpGet(url)));
      Gson gson = new Gson();
      List<LinkedTreeMap> list = gson.fromJson(stream, ArrayList.class);
      SpiraTeamArtifactType[] out = new SpiraTeamArtifactType[list.size()];
      for(int i=0; i<list.size(); i++) {
        LinkedTreeMap map = list.get(i);
        //get the properties from JSON
        int typeId = ((Double)map.get("TaskTypeId")).intValue();
        String typeName = (String)map.get("Name");
        //create a new artifact type
        SpiraTeamArtifactType toAdd = new SpiraTeamArtifactType(typeId, typeName);
        //add the artifact type to the array
        out[i] = toAdd;
      }
      return out;
    }
    catch(IOException e) {
      e.printStackTrace();
      //should never happen
    }
    return new SpiraTeamArtifactType[0];
  }

  /**
   * @return An array with all of the incident types for the given project
   */
  public static SpiraTeamArtifactType[] getIncidentTypes(SpiraTeamCredentials credentials, int projectId) {
    try {
      String url = credentials.getUrl() + restServiceUrl + "projects/" + projectId + "/incidents/types" +
                   "?username=" + credentials.getUsername() + "&api-key=" + credentials.getToken();
      //perform the get request
      BufferedReader stream = new BufferedReader(new InputStreamReader(httpGet(url)));
      Gson gson = new Gson();
      List<LinkedTreeMap> list = gson.fromJson(stream, ArrayList.class);
      SpiraTeamArtifactType[] out = new SpiraTeamArtifactType[list.size()];
      for(int i=0; i<list.size(); i++) {
        LinkedTreeMap map = list.get(i);
        //get the properties from JSON
        int typeId = ((Double)map.get("IncidentTypeId")).intValue();
        String typeName = (String)map.get("Name");
        //create a new artifact type
        SpiraTeamArtifactType toAdd = new SpiraTeamArtifactType(typeId, typeName);
        //add the artifact type to the array
        out[i] = toAdd;
      }
      return out;
    }
    catch(IOException e) {
      e.printStackTrace();
      //should never happen
    }
    return new SpiraTeamArtifactType[0];
  }

  /**
   * Create a new requirement in the system with the given properties and in the given project
   * @param credentials
   * @param body The properties specified to add to the requirement
   * @param projectId The project to create the requirement in
   */
  public static void createRequirement(SpiraTeamCredentials credentials, String body, int projectId) {
    try {
      String url = credentials.getUrl() + restServiceUrl + "projects/" + projectId +
      "/requirements?username=" + credentials.getUsername() + "&api-key=" + credentials.getToken();
      //post the new requirement
      httpPost(url, body);
    }
    catch(IOException e) {
      e.printStackTrace();
      //should never happen
    }
  }

  /**
   * Create a new task in the system with the given properties and in the given project
   * @param credentials
   * @param body The properties specified to add
   * @param projectId The project to create the task in
   */
  public static void createTask(SpiraTeamCredentials credentials, String body, int projectId) {
    try {
      String url = credentials.getUrl() + restServiceUrl + "projects/" + projectId +
      "/tasks?username=" + credentials.getUsername() + "&api-key=" + credentials.getToken();
      //post the new task
      httpPost(url, body);
    }
    catch(IOException e) {
      e.printStackTrace();
      //should never happen
    }
  }

  /**
   * Create a new incident in the system with the given properties and in the givenproject
   * @param credentials
   * @param body The properties specified to add
   * @param projectId The project to create the task in
   */
  public static void createIncident(SpiraTeamCredentials credentials, String body, int projectId) {
    try {
      String url = credentials.getUrl() + restServiceUrl + "projects/" + projectId +
                   "/incidents?username=" + credentials.getUsername() + "&api-key=" + credentials.getToken();
      //post the new task
      httpPost(url, body);
    }
    catch(IOException e) {
      e.printStackTrace();
      //should never happen
    }
  }

  /**
   * Performs an HTTP GET request to the specified URL
   *
   * @param input The URL to perform the query on
   * @return An InputStream containing the JSON returned from the GET request
   * @throws MalformedURLException If the URL is invalid
   */
  public static InputStream httpGet(String input) throws IOException {
    URL url = new URL(input);
    URLConnection connection = url.openConnection();
    //have the connection retrieve JSON
    connection.setRequestProperty("accept", "application/json; charset=utf-8");
    connection.connect();
    return connection.getInputStream();
  }

  /**
   * Performs an HTTP POST request ot the specified URL
   *
   * @param input The URL to perform the query on
   * @param body  The request body to be sent
   * @return An InputStream containing the JSON returned from the POST request
   * @throws IOException
   */
  public static InputStream httpPost(String input, String body) throws IOException {
    URL url = new URL(input);
    HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
    //allow sending a request body
    connection.setDoOutput(true);
    connection.setRequestMethod("POST");
    //have the connection send and retrieve JSON
    connection.setRequestProperty("accept", "application/json; charset=utf-8");
    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    connection.connect();
    //used to send data in the REST request
    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
    //write the body to the stream
    outputStream.writeBytes(body);
    //send the OutputStream to the server
    outputStream.flush();
    outputStream.close();
    //return the input stream
    return connection.getInputStream();
  }
}
