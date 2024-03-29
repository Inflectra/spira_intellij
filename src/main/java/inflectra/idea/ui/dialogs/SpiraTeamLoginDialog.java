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
package inflectra.idea.ui.dialogs;

import inflectra.idea.core.SpiraTeamCredentials;
import inflectra.idea.core.model.artifacts.ArtifactType;
import inflectra.idea.ui.SpiraToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Contains the GUI used for logging in
 * @author Peter Geertsema
 */
public class SpiraTeamLoginDialog extends DialogWrapper {

  private JBTextField url;
  private JBTextField username;
  private JBTextField rssToken;
  private SpiraTeamCredentials credentials;
  private Project project;


  public SpiraTeamLoginDialog(Project project, SpiraTeamCredentials credentials) {
    super(project);
    this.credentials = credentials;
    this.project = project;
    //initialize the dialog
    init();
    setTitle("SpiraTeam Login");
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    //contains all of the labels and text fields
    JBPanel out = new JBPanel();
    //ensuring the components are layed out vertically
    out.setLayout(new BoxLayout(out, BoxLayout.Y_AXIS));
    //URL text field
    url = new JBTextField();
    url.setAlignmentX(0);
    //show the URL stored in credentials if it exists
    if (credentials.getUrl() != null) {
      url.setText(credentials.getUrl());
    }
    out.add(new JBLabel("SpiraTeam URL:"));
    //add spacing between the label and the text field
    out.add(Box.createRigidArea(new Dimension(0, 3)));
    out.add(url);
    //create spacing between them
    out.add(Box.createRigidArea(new Dimension(0,10)));

    username = new JBTextField();
    username.setAlignmentX(0);
    //show the username stored in credentials if it exists
    if (credentials.getUsername() != null) {
      username.setText(credentials.getUsername());
    }
    out.add(new JBLabel("Username:"));
    //add spacing between the label and the text field
    out.add(Box.createRigidArea(new Dimension(0, 3)));
    out.add(username);
    //create spacing between them
    out.add(Box.createRigidArea(new Dimension(0,10)));

    rssToken = new JBTextField();
    rssToken.setAlignmentX(0);
    //show the RSS Token stored in credentials if it exists
    if (credentials.getToken() != null) {
      rssToken.setText(credentials.getToken());
    }
    out.add(new JBLabel("RSS Token:"));
    //add spacing between the label and the text field
    out.add(Box.createRigidArea(new Dimension(0, 3)));
    out.add(rssToken);

    return out;
  }

  /**
   * Called when the OK button is clicked
   */
  @Override
  protected void doOKAction() {
    //if the new username is different from the old one
    if(credentials.getUsername() != null && !(credentials.getUsername().equals(url.getText()))) {
      //reset the last created project and artifact type
      this.credentials.setLastCreatedProjectId(-1);
      this.credentials.setLastOpenArtifactType(ArtifactType.PLACERHOLDER);
    }
    //need to store the new credentials
    this.credentials.setUrl(url.getText());
    this.credentials.setUsername(username.getText());
    this.credentials.setToken(rssToken.getText());
    //reload the SpiraTeam Window
    SpiraToolWindowFactory.reload(project);
    super.doOKAction();
  }

  /**
   * Ensures that the information entered is valid
   */
  @Override
  protected ValidationInfo doValidate() {
    if (url.getText() == null || url.getText().equals("")) {
      return new ValidationInfo("You must enter a URL", url);
    }
    else if(url.getText().endsWith("/")) {
      return new ValidationInfo("Your URL cannot end with a '/'", url);
    }
    if (username.getText() == null || username.getText().equals("")) {
      return new ValidationInfo("You must enter a username", username);
    }
    if (rssToken.getText() == null || rssToken.getText().equals("")) {
      return new ValidationInfo("You must enter an RSS Token", rssToken);
    }
    else if(!(rssToken.getText().endsWith("}"))) {
      return new ValidationInfo("You must include the curly braces in your token", rssToken);
    }
    return null;
  }


  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return this.url;
  }

  /**
   * @return A reference to the rssToken TextField
   */
  public JTextField getRssToken() {
    return this.rssToken;
  }

  /**
   * @return A reference to the url TextField
   */
  public JTextField getUrl() {
    return this.url;
  }

  /**
   * @return A reference to the username TextField
   */
  public JTextField getUsername() {
    return this.username;
  }
}
