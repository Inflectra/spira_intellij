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

import inflectra.idea.core.SpiraTeamUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

/**
 * Used to open the given hyperlink when clicked
 * @author Peter Geertsema
 */
public class HyperlinkListener implements MouseListener {
  URI uri;
  JBLabel label;

  public HyperlinkListener(URI uri, JBLabel label) {
    this.uri = uri;
    this.label = label;
    //make the header have the inactive color by default
    Color color = UIUtil.getHeaderInactiveColor();
    label.setForeground(color);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    //open the url
    SpiraTeamUtil.openURL(uri);
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
   * Uses HTML to add in an underline to the label
   */
  @Override
  public void mouseEntered(MouseEvent e) {
    String oldText = label.getText();
    int startLoc = oldText.indexOf("<h2>");
    int endLoc = oldText.indexOf("</h2>");
    //add in the underline tag between the h2 tags
    String newText = oldText.substring(0, startLoc + 4) + "<u>" +
                     oldText.substring(startLoc+4, endLoc) + "</u>" + oldText.substring(endLoc);
    //apply the changes to the label
    label.setText(newText);
    //set the color to be the active color, depending on the theme
    Color color = UIUtil.getHeaderActiveColor();
    label.setForeground(color);
    //change the cursor
    label.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  /**
   * Removes the HTML which underlines the label
   */
  @Override
  public void mouseExited(MouseEvent e) {
    String oldText = label.getText();
    int startLoc = oldText.indexOf("<u>");
    int endLoc = oldText.indexOf("</u>");
    //remove the underline tags
    String newText = oldText.substring(0, startLoc) + oldText.substring(startLoc+3, endLoc) + oldText.substring(endLoc+4);
    //apply the changes to the label
    label.setText(newText);
    //set the color to be the inactive color, depending on the theme
    Color color = UIUtil.getHeaderInactiveColor();
    label.setForeground(color);
    //change the cursor
    label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }
}