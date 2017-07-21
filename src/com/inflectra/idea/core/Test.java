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

import java.io.FileReader;
import java.util.ArrayList;

/**
 * Test class, not present in the UI whatsoever
 * @author peter.geertsema
 */
public class Test {
  SpiraTeamCredentials credentials;
  public Test() {
    credentials = SpiraTeamCredentials.loadCredentials();
  }
  public static void main(String[] args) {
    try {
      Test test = new Test();
      //test.json();
      //test.credentials();
      test.rest();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void rest() throws Exception {
    /*InputStream inputStream = SpiraTeamUtil.httpGet("https://demo.spiraservice.net/peter-inflectra/services/v5_0/RestService.svc/" +
                                                    "requirements?username=administrator&api-key={AFF91DC8-5C47-4BA4-82C6-C2A971B6F4EB}");
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    Gson gson = new Gson();
    JsonReader jsonReader = new JsonReader(reader);
    ArrayList<LinkedTreeMap> list = gson.fromJson(jsonReader, ArrayList.class);
    for (LinkedTreeMap map : list) {
      System.out.println(map.get("Name"));
    }*/

    /*String data = "[{\"PropertyName\": \"OwnerId\", \"IntValue\": 1}, {\"PropertyName\": \"IncidentStatusId\", \"IntValue\": -2}]";
    InputStream postInputStream = SpiraTeamUtil.httpPost("https://demo.spiraservice.net/peter-inflectra/services" +
    "/v5_0/RestService.svc/projects/1/incidents/search?start_row=1&number_rows=1000&sort_by=Priority" +
    "&username=administrator&api-key={AFF91DC8-5C47-4BA4-82C6-C2A971B6F4EB}", data);
    BufferedReader postReader = new BufferedReader(new InputStreamReader(postInputStream));
    Gson gson = new Gson();
    JsonReader postJsonReader = new JsonReader(postReader);
    ArrayList<LinkedTreeMap> postList = gson.fromJson(postJsonReader, ArrayList.class);
    for (LinkedTreeMap map : postList) {
      System.out.println(map.get("Name"));
    }*/

    SpiraTeamUtil.getAssignedIncidents(credentials);

  }

  private void credentials() {
    SpiraTeamCredentials credentials = SpiraTeamCredentials.loadCredentials();
    System.out.println(credentials);
    credentials.setUrl("https://demo.spiraservice.net/peter-inflectra");
    credentials.setUsername("administrator");
    credentials.setToken("{AFF91DC8-5C47-4BA4-82C6-C2A971B6F4EB}");
    credentials.saveCredentials();
    credentials = SpiraTeamCredentials.loadCredentials();
    System.out.println(credentials);
  }

  private void json() {
    Gson gson = new Gson();
    try {
      JsonReader reader = new JsonReader(new FileReader(this.getClass().getResource("com/inflectra/idea/ui/resources/incidents.json").getPath()));
      ArrayList<LinkedTreeMap> list = gson.fromJson(reader, ArrayList.class);
      for(LinkedTreeMap map: list) {
        System.out.println(map.get("Name"));
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}
