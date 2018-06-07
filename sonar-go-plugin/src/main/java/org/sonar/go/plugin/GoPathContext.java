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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.FileSystem;

class GoPathContext {
  private static final String LINUX_ABSOLUTE_PREFIX = "_/";
  private static final String WINDOWS_ABSOLUTE_PREFIX = "_\\";
  private static final Pattern WINDOWS_ABSOLUTE_REGEX = Pattern.compile("^_\\\\(\\w)_\\\\");
  private static final int MAX_PATH_CACHE_SIZE = 100;

  static final GoPathContext DEFAULT = new GoPathContext(File.separatorChar, File.pathSeparator, System.getenv("GOPATH"));
  final char fileSeparator;
  final List<String> goSrcPathList;
  final Map<String, String> resolvedPaths = new LinkedHashMap<String, String>() {
    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
      return size() > MAX_PATH_CACHE_SIZE;
    }
  };

  GoPathContext(char fileSeparator, String pathSeparator, @Nullable String goPath) {
    this.fileSeparator = fileSeparator;
    List<String> goPathEntries = Collections.emptyList();
    if (goPath != null) {
      goPathEntries = Arrays.asList(goPath.split(pathSeparator));
    }
    this.goSrcPathList = goPathEntries.stream()
      .filter(path -> !path.isEmpty())
      .map(path -> concat(path, "src"))
      .collect(Collectors.toList());
  }

  String concat(String parentPath, String childPath) {
    if (parentPath.isEmpty() || parentPath.charAt(parentPath.length() - 1) == fileSeparator) {
      return parentPath + childPath;
    }
    return parentPath + fileSeparator + childPath;
  }

  /**
   * Try to resolve the absolute path of the given filePath. If filePath is absolute
   * (start with _) return the absolute path without the '_'. If filePath is relative,
   * try to append the first GOPATH entry where this file exists, otherwise return
   * a non-existing absolute path using the first GOPATH entry (or just filePath itself
   * if GOPATH is empty).
   * See {@link GoCoverageReport#findInputFile(String, FileSystem)}
   */
  String resolve(String filePath) {
    return resolvedPaths.computeIfAbsent(filePath, path -> {
      if (path.startsWith(LINUX_ABSOLUTE_PREFIX)) {
        return path.substring(1);
      } else if (path.startsWith(WINDOWS_ABSOLUTE_PREFIX)) {
        Matcher matcher = WINDOWS_ABSOLUTE_REGEX.matcher(path);
        if (matcher.find()) {
          matcher.reset();
          return matcher.replaceFirst("$1:\\\\");
        }
      }
      return prefixByFirstValidGoPath(path)
        .orElseGet(() -> prefixByFirstGoPath(path));
    });
  }

  private Optional<String> prefixByFirstValidGoPath(String filePath) {
    return goSrcPathList.stream()
      .map(goPath -> concat(goPath, filePath))
      .filter(path -> new File(path).exists())
      .findFirst();
  }

  private String prefixByFirstGoPath(String filePath) {
    if (goSrcPathList.isEmpty()) {
      return filePath;
    }
    return concat(goSrcPathList.get(0), filePath);
  }

}
