/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2019 SonarSource SA
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

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;
import org.sonarsource.slang.api.Tree;

public class SlangUnmarshaller {

  private final JsonReader reader;

  public SlangUnmarshaller(Reader reader) throws IOException {
    this.reader = new JsonReader(reader);
    this.reader.setLenient(true);
  }

  Tree readNode() throws IOException {
    //reader.beginObject();
    return null; //TODO: Transform JSON into SlangTree
  }
}
