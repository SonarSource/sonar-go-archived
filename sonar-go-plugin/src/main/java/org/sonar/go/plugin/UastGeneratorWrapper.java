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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.uast.UastNode;

class UastGeneratorWrapper {

  private static final Logger LOG = Loggers.get(UastGeneratorWrapper.class);

  private final ProcessBuilder processBuilder;
  private final ExternalProcessStreamConsumer errorConsumer;

  UastGeneratorWrapper(SensorContext sensorContext) throws IOException {
    this(new DefaultCommand(sensorContext.fileSystem().workDir()));
  }

  UastGeneratorWrapper(Command command) {
    processBuilder = new ProcessBuilder(command.getCommand());
    errorConsumer = new ExternalProcessStreamConsumer();
  }

  UastNode createUast(InputStream source) throws IOException, InterruptedException {
    Process process = processBuilder.start();
    errorConsumer.consumeStream(process.getErrorStream(), LOG::debug);
    try (OutputStream out = process.getOutputStream();
         InputStream in = process.getInputStream()) {
      copy(source, process.getOutputStream());
      out.close();
      UastNode uastNode = UastNode.from(new InputStreamReader(in, StandardCharsets.UTF_8));
      boolean exited = process.waitFor(5, TimeUnit.SECONDS);
      if (exited && process.exitValue() != 0) {
        throw new IllegalStateException("Parser returned non-zero exit value: " + process.exitValue());
      }
      if (process.isAlive()) {
        process.destroyForcibly();
        throw new IllegalStateException("Took too long to parse. External process killed forcibly");
      }
      return uastNode;
    }
  }

  private static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[8192];
    int read;
    while ((read = in.read(buffer)) >= 0) {
      out.write(buffer, 0, read);
    }
  }


  interface Command {
    List<String> getCommand();
  }

  static class DefaultCommand implements Command {

    private final String command;

    DefaultCommand(File workDir) throws IOException {
      command = extract(workDir);
    }

    @Override
    public List<String> getCommand() {
      return Arrays.asList(command, "-");
    }

    private String extract(File workDir) throws IOException {
      String executable = getExecutableForCurrentOS();
      File dest = new File(workDir, executable);
      InputStream streamOfExecutable = getClass().getClassLoader().getResourceAsStream(executable);
      if (streamOfExecutable == null) {
        throw new IllegalStateException(executable + " binary not found on class path");
      }
      try (FileOutputStream destStream = new FileOutputStream(dest);
           InputStream in = streamOfExecutable) {
        copy(in, destStream);
        dest.setExecutable(true);
        return dest.getAbsolutePath();
      }
    }


    private static String getExecutableForCurrentOS() {
      String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
      if (os.contains("win")) {
        return "uast-generator-go-windows-amd64.exe";
      } else if (os.contains("mac")) {
        return "uast-generator-go-darwin-amd64";
      } else {
        return "uast-generator-go-linux-amd64";
      }
    }
  }
}
