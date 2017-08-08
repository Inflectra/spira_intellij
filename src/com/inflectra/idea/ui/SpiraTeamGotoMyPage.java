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
import com.inflectra.idea.core.SpiraTeamUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;

import java.net.URI;

/**
 * Action which, when clicked, opens the user's My Page in browser
 * @author Peter Geertsema
 */
public class SpiraTeamGotoMyPage extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    //get the authentication credentials from the IDE
    SpiraTeamCredentials credentials = ServiceManager.getService(SpiraTeamCredentials.class);
    //create the MyPage URL
    URI myPage = SpiraTeamUtil.getMyPageURL(credentials);
    //open the URL
    SpiraTeamUtil.openURL(myPage);
  }
}
