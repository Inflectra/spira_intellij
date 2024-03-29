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

import inflectra.idea.core.model.artifacts.ArtifactType;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Class which represents the username, RSS token and base URL to access SpiraTeam
 * @author Peter Geertsema
 */
@State(name = "SpiraTeamCredentials", storages =
  @Storage(
    value = "$APP_CONFIG$/credentialspersist.xml")
  )
public class SpiraTeamCredentials implements PersistentStateComponent<SpiraTeamCredentials> {
  /**
   * class used to actually store the credentials
   * needed to have them persist through restarts
   */
  public static class State {
    /**
     * The username of the user
     */
    public String username;
    /**
     * The RSS token of the user
     */
    public String token;
    /**
     * The URL of the user,  ex https://demo.spiraservice.net/test
     */
    public String url;
    /**
     * The type of artifact that was last opened. Used to have an artifact open on startup or refresh
     */
    public ArtifactType lastOpenArtifactType;
    /**
     * The ID of the artifact that was last opened. Used to have an artifact open on startup or refresh
     */
    public int lastOpenArtifactId;
    /**
     * The type of artifact that was last created by the user. Default is placeholder
     */
    public ArtifactType lastCreatedArtifactType = ArtifactType.PLACERHOLDER;
    /**
     * The project Id that the user most recently added a new artifact to
     */
    public int lastCreatedProjectId;

    public State() {

    }

    @Override
    public boolean equals(Object other) {
      if(!(other instanceof State))
        return false;
      State s = (State)other;
      return s.username.equals(username) && s.token.equals(token) && s.url.equals(url);
    }
  }

  /**
   * Stores an instance of a State object, which is a wrapper class for the authentication information
   */
  private State state;

  /**
   * No-arg constructor
   */
  public SpiraTeamCredentials() {
    state = new State();
  }


  @Nullable
  @Override
  public SpiraTeamCredentials getState() {
    //called when the application is closed. The data is then written to the disk
    return this;
  }

  @Override
  public void loadState(SpiraTeamCredentials credentials) {
    //the instance is passed back in when the application is opened, allowing persisting credentials
    XmlSerializerUtil.copyBean(credentials, this);
  }

  //begin getters and setters
  public String getUsername() {
    return state.username;
  }

  public void setUsername(String username) {
    this.state.username = username;
  }
  /**
   * @return The RSS token
   */
  public String getToken() {
    return state.token;
  }

  public void setToken(String token) {
    this.state.token = token;
  }

  public String getUrl() {
    return state.url;
  }

  public void setUrl(String url) {
    this.state.url = url;
  }

  public ArtifactType getLastOpenArtifactType() {
    return this.state.lastOpenArtifactType;
  }

  public void setLastOpenArtifactType(ArtifactType artifactType) {
    this.state.lastOpenArtifactType = artifactType;
  }

  public int getLastOpenArtifactId() {
    return this.state.lastOpenArtifactId;
  }

  public void setLastOpenArtifactId(int artifactId) {
    this.state.lastOpenArtifactId = artifactId;
  }

  public int getLastCreatedProjectId() {
    return state.lastCreatedProjectId;
  }

  public void setLastCreatedProjectId(int projectId) {
    this.state.lastCreatedProjectId = projectId;
  }

  public ArtifactType getLastCreatedArtifactType() {
    return this.state.lastCreatedArtifactType;
  }

  public void setLastCreatedArtifactType(ArtifactType type) {
    this.state.lastCreatedArtifactType = type;
  }

  @Override
  public String toString() {
    return "username: " + getUsername() + " RSS Token: " + getToken() + " URL: " + getUrl();
  }
}
