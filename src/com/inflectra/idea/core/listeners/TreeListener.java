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

import com.inflectra.idea.ui.SpiraToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.UIUtil;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Adds functionality for creating custom trees
 * <p>When the user clicks the given label, the panel passed in with the artifact names is expanded</p>
 * @author Peter Geertsema
 */
public class TreeListener implements MouseListener {
  private static String expandButton = "▶ ";
  private static String collapseButton = "▼ ";
  JBPanel panel;
  JBLabel label;
  boolean isExpanded = false;

  public TreeListener(JBPanel panel, JBLabel label) {
    this.panel = panel;
    this.label = label;
    //add the expand button
    String text = label.getText();
    int startLoc = text.indexOf("<h2>") + 4;
    //add in the expand button, which is smaller than the rest of the text
    text = text.substring(0, startLoc) + "<span style=\"font-size: .6em; font-family: Arial\">" + expandButton + "</span>" + text.substring(startLoc);
    label.setText(text);
    //make the header color inactive by default
    Color color = UIUtil.getHeaderInactiveColor();
    label.setForeground(color);
    //make panel invisible by default
    panel.setVisible(false);
  }

  /**
   * The only method we care about, the others are irrelevant
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    //hide the refresh label
    SpiraToolWindowFactory.hideRefreshLabel();
    //hide the list if it is already expanded
    if (isExpanded) {
      //hide the artifacts
      panel.setVisible(false);
      isExpanded = false;
      //turn the collapse button into an expand button
      String text = label.getText();
      int startLoc = text.indexOf(collapseButton);
      text = text.substring(0, startLoc) + expandButton + text.substring(startLoc + collapseButton.length());
      //apply the changes to the label
      label.setText(text);
      //make the header appear inactive
      Color color = UIUtil.getHeaderInactiveColor();
      label.setForeground(color);
    }
    //show the list if it is not expanded
    else {
      //show the artifacts
      panel.setVisible(true);
      isExpanded = true;
      //turn the expand button into a collapse button
      String text = label.getText();
      int startLoc = text.indexOf(expandButton);
      text = text.substring(0, startLoc) + collapseButton + text.substring(startLoc + expandButton.length());
      //apply the changes to the label
      label.setText(text);
      //make the header color be active
      Color color = UIUtil.getActiveTextColor();
      label.setForeground(color);
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {

  }

  @Override
  public void mouseReleased(MouseEvent e) {

  }

  @Override
  public void mouseEntered(MouseEvent e) {
    Color color = UIUtil.getHeaderActiveColor();
    label.setForeground(color);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    //only change color if it is not expanded
    if(!isExpanded) {
      Color color = UIUtil.getHeaderInactiveColor();
      label.setForeground(color);
    }
    else {
      Color color = UIUtil.getActiveTextColor();
      label.setForeground(color);
    }
  }
}