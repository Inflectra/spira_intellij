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

import com.intellij.openapi.ui.popup.*;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.popup.AbstractPopup;
import com.intellij.util.Processor;

import javax.swing.*;
import java.awt.*;

public class SpiraTeamPopup {
  private boolean isPinned = false;
  private ComponentPopupBuilder popupBuilder;
  private JBPopup popup;
  private JButton pinButton;

  public SpiraTeamPopup(JBPanel panel, JComponent focusOn) {
    popup = buildPopup(panel, focusOn);
    popup.showUnderneathOf(focusOn);
    addButtonListener();
    //addPopupListener();
  }

  private void addPopupListener() {
    popup.addListener(new JBPopupListener() {
      @Override
      public void beforeShown(LightweightWindowEvent event) {
        //do nothing
      }

      @Override
      public void onClosed(LightweightWindowEvent event) {
        if (isPinned) {
          System.out.println("Keeping open");
          popup.cancel();
          Point location = popup.getLocationOnScreen();
          popup = popupBuilder.createPopup();
          popup.showInFocusCenter();
          popup.setLocation(location);
        }
        else {
          System.out.println("Closing Popup");
        }
      }
    });
  }

  /**
   * Adds a listener to the button, allowing two states, 'pin' and 'unpin'
   */
  private void addButtonListener() {
    pinButton.addActionListener(e -> {
      if (!isPinned) {
        pinButton.setText("Unpin");
        isPinned = true;
      }
      else {
        pinButton.setText("Pin");
        isPinned = false;
      }
    });
  }

  /**
   * Creates a popup with the given panel inside
   *
   * @param panel The panel inside the Popup
   * @return
   */
  private JBPopup buildPopup(JBPanel panel, JComponent focusOn) {
    popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, focusOn);
    //allows users to pin the Popup
    pinButton = new JButton("Pin");
    //add the button to the popup
    panel.add(pinButton);
    //allow the user to focus on the popup
    popupBuilder.setFocusable(true);
    //enable the user to resize the popup
    popupBuilder.setResizable(true);
    popupBuilder.setCouldPin(new Processor<JBPopup>() {
      @Override
      public boolean process(JBPopup popup) {
        return true;
      }
    });

    return popupBuilder.createPopup();
  }
}