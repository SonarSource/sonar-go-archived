package org.sonar.go.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

class UastGeneratorWrapper {

  private final File workDir;
  private final ProcessBuilder processBuilder;

  UastGeneratorWrapper(File workDir) throws IOException {
    this.workDir = workDir;
    String command = extract();
    processBuilder = new ProcessBuilder(command, "-");
    processBuilder.redirectErrorStream(true);
  }

  UastNode createUast(InputFile inputFile) throws IOException {
    Process process = processBuilder.start();
    try (OutputStream out = process.getOutputStream();
         InputStream in = process.getInputStream()) {
      copy(inputFile.inputStream(), process.getOutputStream());
      out.close();
      return Uast.from(new InputStreamReader(in, StandardCharsets.UTF_8));
    }
  }

  private String extract() throws IOException {
    String executable = getExecutableForCurrentOS();
    File dest = new File(workDir, executable);
    try (FileOutputStream destStream = new FileOutputStream(dest);
         InputStream streamOfExecutable = getClass().getClassLoader().getResourceAsStream(executable)) {
      copy(streamOfExecutable, destStream);
      setExecutePermission(dest.toPath());
      return dest.getAbsolutePath();
    }
  }

  private void setExecutePermission(Path path) throws IOException {
    PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class);
    Set<PosixFilePermission> permissions = attrs.permissions();
    if (!permissions.contains(PosixFilePermission.OWNER_EXECUTE)) {
      permissions.add(PosixFilePermission.OWNER_EXECUTE);
      Files.setPosixFilePermissions(path, permissions);
    }
  }

  private void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[8192];
    int read;
    while ((read = in.read(buffer)) >= 0) {
      out.write(buffer, 0, read);
    }
  }

  private String getExecutableForCurrentOS() {
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      return "uast-generator-go-windows-amd64.exe";
    } else if (os.contains("mac")) {
      return "uast-generator-go-darwin-amd64";
    } else {
      return "uast-generator-go-linux-amd64";
    }
  }
}
