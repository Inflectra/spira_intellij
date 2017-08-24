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

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * Listener which will make a label disappear when clicked on
 * @author Peter Geertsema
 */
public class DisappearListener implements MouseListener {
  /**
   * The label to disappear
   */
  private JBLabel label;
  /**
   * The panel to remove the label from
   */
  private JBPanel panel;

  public DisappearListener(JBPanel panel, JBLabel label) {
    this.panel = panel;
    this.label = label;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    //remove the label from the panel
    panel.remove(label);
    panel.updateUI();
  }

  @Override
  public void mousePressed(MouseEvent e) {

  }

  @Override
  public void mouseReleased(MouseEvent e) {

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
