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
package com.inflectra.idea.core.listeners;

import com.inflectra.idea.core.SpiraTeamCredentials;
import com.inflectra.idea.core.model.Artifact;
import com.inflectra.idea.ui.SpiraToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows users to click on labels and underlines the label when the user hovers over it
 * @author Peter Geertsema
 */
public class TopLabelMouseListener implements MouseListener {
  private Artifact artifact;
  private JBLabel label;
  /**
   * Used only to show information in the bottom panel when a label is clicked
   */
  private SpiraToolWindowFactory window;
  private SpiraTeamCredentials credentials;

  public TopLabelMouseListener(Artifact artifact, JBLabel label, SpiraToolWindowFactory window, SpiraTeamCredentials credentials) {
    this.artifact = artifact;
    this.label = label;
    this.window = window;
    this.credentials = credentials;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    //show additional information on the artifact in the bottom panel
    window.showInformation(artifact, credentials, label);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    //do nothing
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    //do nothing
  }

  /**
   * Create an underline on the panel when hovered over
   * @param e
   */
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

  /**
   * @return A JBPanel with information regarding to the current artifact
   * @deprecated Popups are no longer used by the SpiraTeam Plugin
   */
  private JBPanel createPanel() {
    JBPanel panel = new JBPanel();
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    //contains the artifact prefix and ID as well as the project
    JBLabel title = new JBLabel(artifact.getPrefix() + ":" + artifact.getArtifactId() + "   Project: " + artifact.getProjectName());
    panel.add(title);
    //contains the description, wrapped in html as Description supports rich text
    panel.add(new JBLabel("<html>Description: " + artifact.getDescription() + "</html>"));
    return panel;

  }

  /**
   * Remove the underline
   */
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
