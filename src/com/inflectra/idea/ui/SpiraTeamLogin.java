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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

public class SpiraTeamLogin extends AnAction {

  private SpiraTeamCredentials credentials;
  public SpiraTeamLogin() {
    super("SpiraTeamLogin");
    credentials = SpiraTeamCredentials.loadCredentials();
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = event.getData(PlatformDataKeys.PROJECT);
    SpiraTeamLoginDialog dialog = new SpiraTeamLoginDialog(project, "SpiraTeam Login", credentials );
    dialog.show();
  }
}