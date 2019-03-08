package org.sonar.go.plugin.externalreport;

import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GolangCILinterReportSensor extends AbstractReportSensor {
    private static final Logger LOG = Loggers.get(GolangCILinterReportSensor.class);

    public static final String PROPERTY_KEY = "sonar.go.golangci-lint.reportPaths";

    private static final Pattern GOLANGCI_LINT_REGEX = Pattern.compile("(?<file>[^:]+):(?<line>\\d+):(?<col>\\d+)\\s*(?<linter>\\S+)\\s*(?<message>.*)");

    @Override
    String linterName() {
        return "GolangCILinter";
    }

    @Override
    String reportsPropertyName() {
        return PROPERTY_KEY;
    }

    @Nullable
    @Override
    ExternalIssue parse(String line) {
        Matcher matcher = GOLANGCI_LINT_REGEX.matcher(line);
        if (matcher.matches()) {
            String linter = mapLinterName(matcher.group("linter").trim());
            String filename = matcher.group("file").trim();
            int lineNumber = Integer.parseInt(matcher.group("line").trim());
            String message = matcher.group("message").trim();

            // Golang CI doesn't currently output the severity or type. so for now
            // we generically adding a default of CODE_SMELL for now
            RuleType type =  RuleType.CODE_SMELL;
            String ruleKey = null;

            return new ExternalIssue(linter, type, ruleKey, filename, lineNumber, message);
        } else {
            LOG.debug(logPrefix() + "Unexpected line: " + line);
        }
        return null;
    }

    private static String mapLinterName(String linter) {
        if ("govet".equals(linter)) {
            return GoVetReportSensor.LINTER_ID;
        }
        return linter;
    }

}
