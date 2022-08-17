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
 * Represents a priority in the SpiraTeam system
 * @author Peter Geertsema
 */
public class SpiraTeamPriority {
  int priorityId;
  String priorityName;

  public SpiraTeamPriority(int priorityId, String priorityName){
    this.priorityId = priorityId;
    this.priorityName = priorityName;
  }

  /**
   * @return The name of this priority
   */
  public String getPriorityName() {
    return priorityName;
  }

  /**
   * @return The ID of this priority
   */
  public int getPriorityId() {
    return priorityId;
  }

  @Override
  public String toString() {
    return getPriorityName();
  }
}
