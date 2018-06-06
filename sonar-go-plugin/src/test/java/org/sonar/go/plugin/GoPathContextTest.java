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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoPathContextTest {

  static String[] tmpDirWithFile = new String[2];
  static String[] existingFileAbsolutePath = new String[2];
  static String tmpDirWithoutFile;
  static String invalidTmpDir;

  @BeforeAll
  static void prepare_temporary_folder() throws IOException {
    String[] suffixes = {"0", "1"};
    for (int i = 0; i < suffixes.length; i++) {
      String suffix = suffixes[i];
      Path dirWithFile = Files.createTempDirectory("tmp_dir_with_file_" + suffix);
      dirWithFile.toFile().deleteOnExit();
      Path srcDir = dirWithFile.resolve("src");
      Files.createDirectory(srcDir);
      Path existingFile = srcDir.resolve("file.go");
      Files.createFile(existingFile);
      tmpDirWithFile[i] = dirWithFile.toAbsolutePath().toString();
      existingFileAbsolutePath[i] = existingFile.toAbsolutePath().toString();
    }

    Path dirWithoutFile = Files.createTempDirectory("tmp_dir_without_file");
    dirWithoutFile.toFile().deleteOnExit();
    Files.createDirectory(dirWithoutFile.resolve("src"));

    tmpDirWithoutFile = dirWithoutFile.toAbsolutePath().toString();
    invalidTmpDir = Paths.get("directory", "not", "found").toString();
  }

  @Test
  void concat_linux() {
    GoPathContext context = new GoPathContext('/', ":", "/home/paul/go");
    assertThat(context.concat("/home", "paul")).isEqualTo("/home/paul");
    assertThat(context.concat("/home/", "paul")).isEqualTo("/home/paul");
    assertThat(context.concat("/", "paul")).isEqualTo("/paul");
    assertThat(context.concat("", "paul")).isEqualTo("paul");
  }

  @Test
  void concat_windows() {
    GoPathContext context = new GoPathContext('\\', ";", "C:\\Users\\paul\\go");
    assertThat(context.concat("C:\\Users", "paul")).isEqualTo("C:\\Users\\paul");
    assertThat(context.concat("C:\\Users\\", "paul")).isEqualTo("C:\\Users\\paul");
    assertThat(context.concat("C:\\", "paul")).isEqualTo("C:\\paul");
    assertThat(context.concat("", "paul")).isEqualTo("paul");
  }

  @Test
  void multiple_go_path_entries_linux() {
    GoPathContext context = new GoPathContext('/', ":", ":/home/paul/go:::/home/luc/go::");
    assertThat(context.goSrcPathList).containsExactly("/home/paul/go/src", "/home/luc/go/src");
  }

  @Test
  void multiple_go_path_entries_windows() {
    GoPathContext context = new GoPathContext('\\', ";", ";;C:\\Users\\paul\\go;C:\\Users\\luc\\go");
    assertThat(context.goSrcPathList).containsExactly("C:\\Users\\paul\\go\\src", "C:\\Users\\luc\\go\\src");
  }

  @Test
  void resolve_existing_file() throws IOException {
    String goPath = tmpDirWithFile[0];
    GoPathContext context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    assertThat(context.resolve("file.go")).isEqualTo(existingFileAbsolutePath[0]);
    assertThat(context.resolvedPaths.keySet()).containsExactly("file.go");
    assertThat(context.resolvedPaths.values()).containsExactly(existingFileAbsolutePath[0]);
  }

  @Test
  void resolve_existing_file_multiple_go_path_entries() throws IOException {
    String goPath = tmpDirWithFile[0] + File.pathSeparator + tmpDirWithFile[1];
    GoPathContext context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    assertThat(context.resolve("file.go")).isEqualTo(existingFileAbsolutePath[0]);
    assertThat(context.resolvedPaths.keySet()).containsExactly("file.go");
    assertThat(context.resolvedPaths.values()).containsExactly(existingFileAbsolutePath[0]);
  }

  @Test
  void resolve_existing_file_in_the_first_go_path_entry() throws IOException {
    String goPath = tmpDirWithFile[0] + File.pathSeparator + tmpDirWithoutFile + File.pathSeparator + invalidTmpDir;
    GoPathContext context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    assertThat(context.resolve("file.go")).isEqualTo(existingFileAbsolutePath[0]);
  }

  @Test
  void resolve_existing_file_in_the_last_go_path_entry() throws IOException {
    String goPath = invalidTmpDir + File.pathSeparator + tmpDirWithoutFile + File.pathSeparator + tmpDirWithFile[0];
    GoPathContext context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    assertThat(context.resolve("file.go")).isEqualTo(existingFileAbsolutePath[0]);
  }

  @Test
  void resolve_none_existing_file_with_multiple_go_path_entries() throws IOException {
    String goPath = tmpDirWithoutFile + File.pathSeparator + invalidTmpDir;
    GoPathContext context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    String expected = tmpDirWithoutFile + File.separatorChar + "src" + File.separatorChar + "file.go";
    assertThat(context.resolve("file.go")).isEqualTo(expected);
  }

  @Test
  void resolve_none_existing_file_with_empty_go_path() throws IOException {
    String goPath = File.pathSeparator + File.pathSeparator;
    GoPathContext context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    assertThat(context.resolve("file.go")).isEqualTo("file.go");
  }

  @Test
  void resolve_none_existing_file_with_null_go_path() throws IOException {
    String goPath = null;
    GoPathContext context = new GoPathContext(File.separatorChar, File.pathSeparator, goPath);
    assertThat(context.resolve("file.go")).isEqualTo("file.go");
  }

  @Test
  void resolve_absolute_path_linux() throws IOException {
    GoPathContext context = new GoPathContext('/', ":", "/home/paul/go");
    assertThat(context.resolve("_/my-app/my-app.go")).isEqualTo("/my-app/my-app.go");
  }

  @Test
  void resolve_absolute_path_windows() throws IOException {
    GoPathContext context = new GoPathContext('\\', ";", "C:\\Users\\paul\\go");
    assertThat(context.resolve("_\\C_\\my-app\\my-app.go")).isEqualTo("C:\\my-app\\my-app.go");
  }

}
