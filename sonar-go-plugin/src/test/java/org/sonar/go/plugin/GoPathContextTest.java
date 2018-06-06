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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoPathContextTest {

  @Test
  void concat() {
    GoPathContext linuxContext = new GoPathContext('/', ":", "/home/paul/go");
    GoPathContext windowsContext = new GoPathContext('\\', ";", "C:\\Users\\paul\\go");

    assertThat(linuxContext.concat("/home", "paul")).isEqualTo("/home/paul");
    assertThat(linuxContext.concat("/home/", "paul")).isEqualTo("/home/paul");
    assertThat(linuxContext.concat("/", "paul")).isEqualTo("/paul");
    assertThat(linuxContext.concat("", "paul")).isEqualTo("paul");

    assertThat(windowsContext.concat("C:\\Users", "paul")).isEqualTo("C:\\Users\\paul");
    assertThat(windowsContext.concat("C:\\Users\\", "paul")).isEqualTo("C:\\Users\\paul");
    assertThat(windowsContext.concat("C:\\", "paul")).isEqualTo("C:\\paul");
    assertThat(windowsContext.concat("", "paul")).isEqualTo("paul");
  }

  @Test
  void source_directories() {
    GoPathContext linuxContext = new GoPathContext('/', ":", "/home/paul/go:/home/luc/go");
    assertThat(linuxContext.goSrcPathList).containsExactly("/home/paul/go/src", "/home/luc/go/src");

    GoPathContext windowsContext = new GoPathContext('\\', ";", "C:\\Users\\paul\\go;C:\\Users\\luc\\go");
    assertThat(windowsContext.goSrcPathList).containsExactly("C:\\Users\\paul\\go\\src", "C:\\Users\\luc\\go\\src");
  }

  @Test
  void resolve_using_go_path() throws IOException {
    Path tmpGoDir = Files.createTempDirectory("tmp_go_path");
    tmpGoDir.toFile().deleteOnExit();
    Path srcDir = tmpGoDir.resolve("src");
    Files.createDirectory(srcDir);
    Files.createFile(srcDir.resolve("file.go"));

    String validGoPath = tmpGoDir.toAbsolutePath().toString();
    String invalidPath1 = Paths.get("directory", "not", "found", "one").toString();
    String invalidPath2 = Paths.get("directory", "not", "found", "two").toString();

    String goPath = validGoPath;
    GoPathContext context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    String expected = validGoPath + File.separatorChar + "src" + File.separatorChar + "file.go";
    assertThat(context.resolveUsingGoPath("file.go")).isEqualTo(expected);
    assertThat(context.resolvedPaths.keySet()).containsExactly("file.go");
    assertThat(context.resolvedPaths.values()).containsExactly(expected);

    goPath = invalidPath1 + File.pathSeparator + invalidPath2 + File.pathSeparator + validGoPath;
    context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    expected = validGoPath + File.separatorChar + "src" + File.separatorChar + "file.go";
    assertThat(context.resolveUsingGoPath("file.go")).isEqualTo(expected);

    goPath = validGoPath + File.pathSeparator + invalidPath1;
    context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    expected = validGoPath + File.separatorChar + "src" + File.separatorChar + "file.go";
    assertThat(context.resolveUsingGoPath("file.go")).isEqualTo(expected);

    goPath = invalidPath1 + File.pathSeparator + invalidPath2;
    context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    expected = invalidPath1 + File.separatorChar + "src" + File.separatorChar + "file.go";
    assertThat(context.resolveUsingGoPath("file.go")).isEqualTo(expected);

    goPath = File.pathSeparator + File.pathSeparator;
    context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    expected = "file.go";
    assertThat(context.resolveUsingGoPath("file.go")).isEqualTo(expected);

    goPath = null;
    context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    expected = "file.go";
    assertThat(context.resolveUsingGoPath("file.go")).isEqualTo(expected);
  }

  @Test
  void resolve_absolute_path() throws IOException {
    GoPathContext linuxContext = new GoPathContext('/', ":", "/home/paul/go");
    assertThat(linuxContext.resolveUsingGoPath("_/my-app/my-app.go")).isEqualTo("/my-app/my-app.go");
    assertThat(linuxContext.resolveUsingGoPath("my-app/my-app.go")).isEqualTo("/home/paul/go/src/my-app/my-app.go");

    GoPathContext windowsContext = new GoPathContext('\\', ";", "C:\\Users\\paul\\go");
    assertThat(windowsContext.resolveUsingGoPath("_\\C_\\my-app\\my-app.go")).isEqualTo("C:\\my-app\\my-app.go");
    assertThat(windowsContext.resolveUsingGoPath("my-app\\my-app.go")).isEqualTo("C:\\Users\\paul\\go\\src\\my-app\\my-app.go");
  }

}
