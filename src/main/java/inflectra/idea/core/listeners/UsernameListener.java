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
package inflectra.idea.core.listeners;

import inflectra.idea.core.SpiraTeamCredentials;
import inflectra.idea.ui.dialogs.SpiraTeamLoginDialog;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to open the log-in dialog when the label is clicked
 * @author Peter Geertsema
 */
public class UsernameListener implements MouseListener {
  private SpiraTeamCredentials credentials;
  private Project project;
  private JBLabel label;

  public UsernameListener(SpiraTeamCredentials credentials, Project project, JBLabel label) {
    this.credentials = credentials;
    this.project = project;
    this.label = label;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    //create a new login dialog
    SpiraTeamLoginDialog dialog = new SpiraTeamLoginDialog(project, credentials);
    dialog.show();
  }

  @Override
  public void mousePressed(MouseEvent e) {
    //do nothing
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    //do nothing
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    Font font = label.getFont();
    //create a Map with the attributes of the font
    Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
    //turning on the underline
    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    //setting the new font
    label.setFont(font.deriveFont(attributes));
    //set the cursor to the hand
    label.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  @Override
  public void mouseExited(MouseEvent e) {
    Font font = label.getFont();
    Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
    //-1 is the constant for no underline
    attributes.put(TextAttribute.UNDERLINE, -1);
    //setting the new font
    label.setFont(font.deriveFont(attributes));
    //set the cursor back to normal
    label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }
}
