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
import com.inflectra.idea.core.model.Artifact;
import com.inflectra.idea.core.model.ArtifactType;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Class with a wide variety of utility methods used throughout the plug-in
 *
 * @author peter.geertsema
 */
public class SpiraTeamUtil {
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
    String url = credentials.getUrl() + "/services/v5_0/RestService.svc/requirements?username=" + credentials.getUsername() +
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
    String url = credentials.getUrl() + "/services/v5_0/RestService.svc/tasks?username=" + credentials.getUsername() +
                 "&api-key=" + credentials.getToken();
    //perform the GET request
    return httpGet(url);
  }

  /**
   * @param credentials The information needed to perform the HTTP request
   * @return A list of all the Incidents, each Map represents an incident
   * @throws IOException If the URL is invalid
   */
  public static ArrayList<LinkedTreeMap> getAssignedIncidents(SpiraTeamCredentials credentials) throws IOException {
    //TODO: Update to use the /incidents REST request when Version 5.3 is released
    //the body of the request
    String body = "[{\"PropertyName\": \"OwnerId\", \"IntValue\": 1}, {\"PropertyName\": \"IncidentStatusId\", \"IntValue\": -2}]";
    //the list to be returned
    ArrayList<LinkedTreeMap> out = new ArrayList<>();
    //get all of the projects available to the user
    ArrayList<Integer> projectIds = getAvailableProjects(credentials);
    //loop through all of the available projects
    for (int projectId : projectIds) {
      //build the URL for each project
      String url = credentials.getUrl() +
                   "/services/v5_0/RestService.svc/projects/" + projectId + "/incidents/search" +
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
  private static ArrayList<Integer> getAvailableProjects(SpiraTeamCredentials credentials) throws IOException {
    //create the URL
    String url = credentials.getUrl() + "/services/v5_0/RestService.svc/projects?username="
                 + credentials.getUsername() + "&api-key=" + credentials.getToken();
    //perform an HTTP GET request on the specified URL
    InputStream stream = httpGet(url);
    Gson gson = new Gson();
    //create a JSON reader from the JSON sent from the GET request
    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(stream)));
    //turn JSON into an ArrayList
    ArrayList<LinkedTreeMap> jsonList = gson.fromJson(reader, ArrayList.class);
    //the list to be returned
    ArrayList<Integer> out = new ArrayList<>();
    //loop through each map in the list
    for (LinkedTreeMap map : jsonList) {
      //retrieve the project ID
      Double toAdd = (Double)map.get("ProjectId");
      //add the ID to the output list
      out.add(toAdd.intValue());
    }
    return out;
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
