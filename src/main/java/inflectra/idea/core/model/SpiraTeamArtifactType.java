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
package inflectra.idea.core.model;

/**
 * The type of requirement, task or incident example Bug, Change Request, etc
 * @author Peter Geertsema
 */
public class SpiraTeamArtifactType {
  /**
   * The ID of the type
   */
  int typeId;
  /**
   * The name of the type
   */
  String name;
  public SpiraTeamArtifactType(int typeId, String name) {
    this.typeId = typeId;
    this.name = name;
  }

  public int getTypeId() {
    return typeId;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
