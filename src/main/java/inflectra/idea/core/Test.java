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
package inflectra.idea.core;


import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Test class, not present in the UI whatsoever
 *
 * @author Peter Geertsema
 */
public class Test {
  static String expandButton = "▶";
  static String collapseButton = "▼";

  SpiraTeamCredentials credentials;

  public Test() {
    //credentials = SpiraTeamCredentials.loadCredentials();
  }

  public static void main(String[] args) {
    try {
      Test test = new Test();
      //test.json();
      //test.credentials();
      //test.rest();
      //test.scroll();
      //test.underline();
      test.expand();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void expand() {
    String text = "<html><h2>Text</h2></html>";
    int startLoc = text.indexOf("<h2>") + 4;
    text = text.substring(0, startLoc) + expandButton + text.substring(startLoc);
    System.out.println(text);

    startLoc = text.indexOf(expandButton);
    text = text.substring(0, startLoc) + collapseButton + text.substring(startLoc + 1);
    System.out.println(text);

    startLoc = text.indexOf(collapseButton);
    text = text.substring(0, startLoc) + expandButton + text.substring(startLoc + 1);
    System.out.println(text);
  }

  private void underline() {
    String s = "<HTML><h2>Test</h2></HTML>";
    int startLoc = s.indexOf("<h2>");
    int endLoc = s.indexOf("</h2>");
    s = s.substring(0, startLoc + 4) + "<u>" + s.substring(startLoc+4, endLoc) + "</u>" + s.substring(endLoc);

    startLoc = s.indexOf("<u>");
    endLoc = s.indexOf("</u>");
    s = s.substring(0, startLoc) + s.substring(startLoc+3, endLoc) + s.substring(endLoc+4);
    System.out.println(s);
  }

  private void scroll() {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);

    JBPanel panel = new JBPanel();
    Container c = frame.getContentPane();
    panel.setSize(100,100);
    panel.setLayout(new GridLayout(1000, 1));
    for(int i = 0; i<1000;i++)
      panel.add(new JBLabel("Label " + i));

    JScrollPane jsp = new JBScrollPane(panel);
    c.add(jsp);
    frame.setSize(100,100);
    frame.setVisible(true);
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
