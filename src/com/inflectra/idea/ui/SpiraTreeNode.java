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

import com.intellij.ui.treeStructure.PatchedDefaultMutableTreeNode;

import java.net.URI;

/**
 * Node to be used in tree's which simply adds the ability to show additional information on click
 *
 * @author peter.geertsema
 */
public class SpiraTreeNode extends PatchedDefaultMutableTreeNode {
  private URI uri;

  /**
   * @param uri The URI which leads to the artifact on the web
   */
  public SpiraTreeNode(Object userObject, URI uri) {
    super(userObject);
    this.uri = uri;
  }

  public URI getUri() {
    return uri;
  }
}
