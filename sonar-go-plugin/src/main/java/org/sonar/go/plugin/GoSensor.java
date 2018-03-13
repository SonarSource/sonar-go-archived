package org.sonar.go.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

public class GoSensor implements Sensor {

  private final Checks<Check> checks;

  public GoSensor(CheckFactory checkFactory) {
    checks = checkFactory.<Check>create(GoRulesDefinition.REPOSITORY_KEY)
      .addAnnotatedChecks((Iterable) GoChecks.getChecks());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(GoLanguage.KEY)
      .name("SonarGo");
  }

  @Override
  public void execute(SensorContext context) {
    Engine ruleEngine = new Engine(checks.all());
    try {
      String uastGeneratorGo = uastGeneratorGo(context);
      for (InputFile inputFile : getInputFiles(context)) {
        UastNode uast = createUast(uastGeneratorGo, inputFile.contents());
        Engine.ScanResult scanResult = ruleEngine.scan(uast);
        scanResult.issues.forEach(issue -> {
          NewIssue newIssue = context.newIssue();
          TextRange textRange = inputFile.selectLine(issue.getLine());// TODO should be more precise
          newIssue
            .at(newIssue.newLocation().on(inputFile).at(textRange).message(issue.getMessage()))
            .forRule(checks.ruleKey(issue.getRule()))
            .save();
        });
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Iterable<InputFile> getInputFiles(SensorContext context) {
    FileSystem fs = context.fileSystem();
    return fs.inputFiles(fs.predicates().hasLanguage(GoLanguage.KEY));
  }

  private String uastGeneratorGo(SensorContext context) throws IOException {
    File workDir = context.fileSystem().workDir();
    String binary = getBinaryForCurrentOS();
    File dest = new File(workDir, binary);
    try (FileOutputStream outputStream = new FileOutputStream(dest);
         InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(binary)) {
      byte[] buffer = new byte[resourceAsStream.available()];
      int read;
      while ((read = resourceAsStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, read);
      }
      return dest.getAbsolutePath();
    }
  }

  private String getBinaryForCurrentOS() {
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      return "uast-generator-go-windows-amd64.exe";
    } else if (os.contains("mac")) {
      return "uast-generator-go-darwin-amd64";
    } else {
      return "uast-generator-go-linux-amd64";
    }
  }

  private UastNode createUast(String command, String fileContent) throws IOException {
    ProcessBuilder builder = new ProcessBuilder(command, "-");
    builder.redirectErrorStream(true);
    Process process = builder.start();
    try (OutputStreamWriter out = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
         InputStream in = process.getInputStream()) {
      out.write(fileContent);
      out.close();
      return Uast.from(new InputStreamReader(in, StandardCharsets.UTF_8));
    }
  }
}
