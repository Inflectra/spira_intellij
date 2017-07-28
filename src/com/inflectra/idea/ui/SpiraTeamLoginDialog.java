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

import com.inflectra.idea.core.SpiraTeamCredentials;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SpiraTeamLoginDialog extends DialogWrapper {

  private JTextField url;
  private JTextField username;
  private JTextField rssToken;
  private SpiraTeamCredentials credentials;
  private Project project;


  public SpiraTeamLoginDialog(Project project, String title, SpiraTeamCredentials credentials) {
    super(project);
    this.credentials = credentials;
    this.project = project;
    init();
    setTitle(title);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    //contains all of the labels and text fields
    JPanel out = new JPanel();
    //ensuring the components are layed out vertically
    out.setLayout(new BoxLayout(out, BoxLayout.Y_AXIS));

    url = new JTextField(30);
    if (credentials.getUrl() != null) {
      url.setText(credentials.getUrl());
    }
    out.add(new JLabel("SpiraTeam URL:"));
    out.add(url);
    username = new JTextField(30);
    if (credentials.getUsername() != null) {
      username.setText(credentials.getUsername());
    }
    out.add(new JLabel("Username:"));
    out.add(username);
    rssToken = new JTextField(30);
    if (credentials.getToken() != null) {
      rssToken.setText(credentials.getToken());
    }
    out.add(new JLabel("RSS Token:"));
    out.add(rssToken);

    return out;
  }

  @Override
  protected void doOKAction() {
    this.credentials.setUrl(url.getText());
    this.credentials.setUsername(username.getText());
    this.credentials.setToken(rssToken.getText());
    SpiraToolWindowFactory.reload(project);
    super.doOKAction();
  }

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
