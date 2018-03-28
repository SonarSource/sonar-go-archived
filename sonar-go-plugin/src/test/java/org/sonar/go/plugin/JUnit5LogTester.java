/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.go.plugin;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.sonar.api.utils.log.LogTester;

/**
 * JUnit 5 extension model has changed. This is to adapt {@link LogTester} to Junit 5 API.
 * Note that we have to depend on {@code junit-jupiter-migrationsupport} to have JUnit 4 dependencies available.
 * Eventually this should be moved to sonar-plugin-api
 */
public class JUnit5LogTester extends LogTester implements BeforeAllCallback, AfterAllCallback {

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    after();
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    try {
      before();
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
  }
}
