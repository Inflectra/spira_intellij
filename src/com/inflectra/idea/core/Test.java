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
 *
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
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void rest() throws Exception {
    SpiraTeamUtil.getAssignedIncidents(credentials);
  }

  private void json() {
    Gson gson = new Gson();
    try {
      JsonReader reader =
        new JsonReader(new FileReader(this.getClass().getResource("com/inflectra/idea/ui/resources/incidents.json").getPath()));
      ArrayList<LinkedTreeMap> list = gson.fromJson(reader, ArrayList.class);
      for (LinkedTreeMap map : list) {
        System.out.println(map.get("Name"));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
