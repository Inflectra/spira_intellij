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
package com.inflectra.idea.core.model;

/**
 * Represents a user in SpiraTeam
 * @author Peter Geertsema
 */
public class SpiraTeamUser {
  private String fullName;
  private int userId;
  private String username;
  private int roleId;

  public SpiraTeamUser(String fullName, int userId, String username, int roleId) {
    this.fullName = fullName;
    this.userId = userId;
    this.username = username;
    this.roleId = roleId;
  }

  public void setRoleId(int r) {
    roleId = r;
  }
  public int getRoleId() {
    return roleId;
  }
  /**
   * @return The full name of the user
   */
  public String getFullName() {
    return fullName;
  }
  /**
   * @return The user ID of the user
   */
  public int getUserId() {
    return userId;
  }

  /**
   * @return The username of the user
   */
  public String getUsername() {
    return username;
  }


  @Override
  public String toString() {
    return getFullName();
  }
}
