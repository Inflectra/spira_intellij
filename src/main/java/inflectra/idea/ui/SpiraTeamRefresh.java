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
package inflectra.idea.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Action which, when clicked, refreshes the artifacts in SpiraTeam from the copy on the server
 * @author Peter Geertsema
 */
public class SpiraTeamRefresh extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    //refresh the top and bottom panels
    SpiraToolWindowFactory.reload(e.getProject());
  }
}
