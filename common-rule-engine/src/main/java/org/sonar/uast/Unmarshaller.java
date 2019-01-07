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
package org.sonar.uast;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

class Unmarshaller {

  private static final Map<String, UastNode.Kind> KIND_MAP = Arrays.stream(UastNode.Kind.values())
    .collect(Collectors.toMap(Enum::name, Function.identity()));

  private final JsonReader reader;

  private Unmarshaller(Reader reader) {
    this.reader = new JsonReader(reader);
    this.reader.setLenient(true);
  }

  static UastNode unmarshal(Reader reader) throws IOException {
    Unmarshaller unmarshaller = new Unmarshaller(reader);
    return unmarshaller.readNode();
  }

  private UastNode readNode() throws IOException {
    Set<UastNode.Kind> kinds = Collections.emptySet();
    String nativeNode = "";
    UastNode.Token token = null;
    List<UastNode> children = Collections.emptyList();
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "kinds":
          kinds = readKinds();
          break;
        case "nativeNode":
          nativeNode = readNullable(reader::nextString, "");
          break;
        case "token":
          token = readNullable(this::readToken, null);
          break;
        case "children":
          children = readNullable(this::readChildren, Collections.emptyList());
          break;
        default:
          reader.skipValue();
          break;
      }
    }
    reader.endObject();
    return new UastNode(kinds, nativeNode, token, children);
  }

  private List<UastNode> readChildren() throws IOException {
    List<UastNode> children = new ArrayList<>();
    reader.beginArray();
    while (reader.hasNext()) {
      UastNode uastNode = readNode();
      children.add(uastNode);
    }
    reader.endArray();
    return children;
  }

  private UastNode.Token readToken() throws IOException {
    int line = -1;
    int column = -1;
    String value = "";
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "value":
          value = readNullable(reader::nextString, "");
          break;
        case "line":
          line = reader.nextInt();
          break;
        case "column":
          column = reader.nextInt();
          break;
        default:
          reader.skipValue();
          break;
      }
    }
    reader.endObject();
    if (line == -1 || column == -1) {
      throw new JsonParseException("Attributes 'line' and 'column' are mandatory on 'token' object.");
    }
    return new UastNode.Token(line, column, value);
  }

  private Set<UastNode.Kind> readKinds() throws IOException {
    Set<UastNode.Kind> kinds = EnumSet.noneOf(UastNode.Kind.class);
    reader.beginArray();
    while (reader.hasNext()) {
      String kindAsString = reader.nextString();
      UastNode.Kind kind = KIND_MAP.get(kindAsString);
      if (kind != null) {
        kinds.add(kind);
      }
    }
    reader.endArray();
    return kinds;
  }

  @FunctionalInterface
  interface SupplierEx<T> {
    T get() throws IOException;
  }

  private <T> T readNullable(SupplierEx<T> supplierEx, @Nullable T defaultValue) {
    try {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull();
        return defaultValue;
      } else {
        return supplierEx.get();
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
