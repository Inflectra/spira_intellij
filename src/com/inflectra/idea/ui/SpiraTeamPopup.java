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

import com.inflectra.idea.core.model.Artifact;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.popup.AbstractPopup;
import com.intellij.util.Processor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpiraTeamPopup {
  private boolean isPinned = false;
  private ComponentPopupBuilder popupBuilder;
  private JBPopup popup;
  /**
   * Used only for equals methods, not actually changed at all
   */
  private Artifact artifact;
  /**
   * The currently active popup
   */
  private static SpiraTeamPopup openPopup;

  public SpiraTeamPopup(JBPanel panel, JComponent focusOn, Artifact artifact) {
    this.artifact = artifact;
    popup = buildPopup(panel, focusOn);
    if(openPopup != null && openPopup.equals(this)) {
      //do nothing as the old popup is identical to the old one
    }
    else {
      prepareNewPopup();
      //show the popup
      popup.showUnderneathOf(focusOn);
    }
  }

  /**
   * Sets the newly open as the active popup and closes the previously open one
   */
  private void prepareNewPopup() {
    if(openPopup != null) {
      //cancel the popup
      openPopup.cancel();
      openPopup = null;
    }
    openPopup = this;
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
        }
        else {
          System.out.println("Closing Popup");
        }
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

    popupBuilder.setCouldPin(new Processor<JBPopup>() {
      @Override
      public boolean process(JBPopup popup) {
        popup.moveToFitScreen();
        return true;
      }
    });

    //allow the user to focus on the popup
    popupBuilder.setFocusable(true);
    //enable the user to resize the popup
    popupBuilder.setResizable(true);
    return popupBuilder.createPopup();
  }

  public void cancel() {
    popup.cancel();
  }

  @Override
  public boolean equals(Object other) {
    if(!(other instanceof SpiraTeamPopup))
      return false;
    //only true if the
    return ((SpiraTeamPopup)other).artifact.equals(this.artifact);
  }
}