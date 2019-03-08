package org.sonar.go.plugin.externalreport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.go.plugin.JUnit5LogTester;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.go.plugin.externalreport.ExternalLinterSensorHelper.REPORT_BASE_PATH;

public class GolangCILinterSensorTest {
    @RegisterExtension
    static JUnit5LogTester logTester = new JUnit5LogTester();

    @BeforeEach
    void setUp() {
        logTester.clear();
    }

    @Test
    public void test_descriptor() {
        DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
        new GolangCILinterReportSensor().describe(sensorDescriptor);
        assertThat(sensorDescriptor.name()).isEqualTo("Import of GolangCILinter issues");
        assertThat(sensorDescriptor.languages()).containsOnly("go");
    }

    @Test
    void issues_with_sonarqube_72() throws IOException {
        SensorContextTester context = ExternalLinterSensorHelper.createContext(7, 2);
        context.settings().setProperty("sonar.go.golangci-lint.reportPaths", REPORT_BASE_PATH.resolve("golangci-lint-report.txt").toString());
        List<ExternalIssue> externalIssues = ExternalLinterSensorHelper.executeSensor(new GolangCILinterReportSensor(), context);
        assertThat(externalIssues).hasSize(5);

        org.sonar.api.batch.sensor.issue.ExternalIssue first = externalIssues.get(0);
        assertThat(first.type()).isEqualTo(RuleType.CODE_SMELL);
        assertThat(first.ruleKey().repository()).isEqualTo("errcheck");
        assertThat(first.primaryLocation().message()).isEqualTo("Error return value of `(*encoding/json.Encoder).Encode` is not checked");
        assertThat(first.primaryLocation().textRange().start().line()).isEqualTo(5);

        org.sonar.api.batch.sensor.issue.ExternalIssue second = externalIssues.get(1);
        assertThat(second.type()).isEqualTo(RuleType.CODE_SMELL);
        assertThat(second.ruleKey().repository()).isEqualTo("errcheck");
        assertThat(second.primaryLocation().message()).isEqualTo("Error return value of `(*encoding/json.Encoder).Encode` is not checked");
        assertThat(second.primaryLocation().textRange().start().line()).isEqualTo(3);

        org.sonar.api.batch.sensor.issue.ExternalIssue third = externalIssues.get(2);
        assertThat(third.type()).isEqualTo(RuleType.CODE_SMELL);
        assertThat(third.ruleKey().repository()).isEqualTo("govet");
        assertThat(third.ruleKey().rule()).isEqualTo("issue");
        assertThat(third.primaryLocation().message()).isEqualTo("Errorf format %v reads arg #1, but call has 0 args");
        assertThat(third.primaryLocation().textRange().start().line()).isEqualTo(2);



        org.sonar.api.batch.sensor.issue.ExternalIssue forth = externalIssues.get(3);
        assertThat(forth.type()).isEqualTo(RuleType.CODE_SMELL);
        assertThat(forth.ruleKey().repository()).isEqualTo("errcheck");
        assertThat(forth.primaryLocation().message()).isEqualTo("Error return value of `(*encoding/json.Encoder).Encode` is not checked");
        assertThat(forth.primaryLocation().textRange().start().line()).isEqualTo(1);

        org.sonar.api.batch.sensor.issue.ExternalIssue fifth = externalIssues.get(4);
        assertThat(fifth.type()).isEqualTo(RuleType.CODE_SMELL);
        assertThat(fifth.ruleKey().repository()).isEqualTo("staticcheck");
        assertThat(fifth.primaryLocation().message()).isEqualTo("session.New is deprecated: Use NewSession functions to create sessions instead. NewSession has the same functionality as New except an error can be returned when the func is called instead of waiting to receive an error until a request is made.");
        assertThat(fifth.primaryLocation().textRange().start().line()).isEqualTo(2);

        assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
    }

    @Test
    void no_issues_with_invalid_report_line() throws IOException {
        SensorContextTester context = ExternalLinterSensorHelper.createContext(7, 2);
        context.settings().setProperty("sonar.go.golangci-lint.reportPaths", REPORT_BASE_PATH.resolve("golangci-lint-with-error.txt").toString());
        List<ExternalIssue> externalIssues = ExternalLinterSensorHelper.executeSensor(new GolangCILinterReportSensor(), context);
        assertThat(externalIssues).hasSize(1);
        assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(0);
        assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(1);
        assertThat(logTester.logs(LoggerLevel.DEBUG).get(0)).startsWith("GolangCILinterReportSensor: Unexpected line: thisisaninvalidline");
    }


}
