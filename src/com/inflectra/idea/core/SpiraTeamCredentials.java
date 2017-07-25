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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Class which represents the username, RSS token and base URL to access SpiraTeam
 *
 * @author peter.geertsema
 */
@State(name = "SpiraTeamCredentials",
  storages = {
    @Storage(StoragePathMacros.WORKSPACE_FILE)
  })
public class SpiraTeamCredentials implements PersistentStateComponent<SpiraTeamCredentials.State> {
  class State {
    public State() {
    }

    /**
     * The username of the user
     */
    private String username;
    /**
     * The RSS token of the user
     */
    private String token;
    /**
     * The URL of the user,  ex https://demo.spiraservice.net/test
     */
    private String url;

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof State)) {
        return false;
      }
      State otherS = (State)other;
      return username.equals(otherS.username) && token.equals(otherS.token) && url.equals(otherS.token);
    }
  }

  State state;

  /**
   * No-arg constructor
   */
  public SpiraTeamCredentials() {
    state = new State();
  }

  public static SpiraTeamCredentials loadCredentials() {
    SpiraTeamCredentials out = new SpiraTeamCredentials();
    try {
      Scanner reader = new Scanner(new File(SpiraTeamCredentials.class.getResource("credentials.txt").getPath()));
      out.setUrl(reader.nextLine());
      out.setUsername(reader.nextLine());
      out.setToken(reader.nextLine());
      reader.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return out;
  }

  public void saveCredentials() {
    try {
      PrintWriter writer = new PrintWriter(getClass().getResource("credentials.txt").getPath());
      writer.println(this.getUrl());
      writer.println(this.getUsername());
      writer.println(this.getToken());
      writer.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*@Override
  public void initComponent() {
    // TODO: insert component initialization logic here
  }

  @Override
  public void disposeComponent() {
    // TODO: insert component disposal logic here
  }

  @Override
  @NotNull
  public String getComponentName() {
    return "SpiraTeamCredentials";
  }*/

  @Nullable
  @Override
  public SpiraTeamCredentials.State getState() {
    return state;
  }

  @Override
  public void loadState(SpiraTeamCredentials.State state) {
    XmlSerializerUtil.copyBean(state, this);
  }


  public String getUsername() {
    return this.state.username;
  }

  public void setUsername(String username) {
    this.state.username = username;
  }

  public String getToken() {
    return this.state.token;
  }

  public void setToken(String token) {
    this.state.token = token;
  }

  public String getUrl() {
    return this.state.url;
  }

  public void setUrl(String url) {
    this.state.url = url;
  }

  @Override
  public String toString() {
    return "username: " + this.state.username + " RSS Token: " + this.state.token + " URL: " + this.state.url;
  }
}
