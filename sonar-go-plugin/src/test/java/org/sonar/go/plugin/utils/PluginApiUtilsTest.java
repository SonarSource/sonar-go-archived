package org.sonar.go.plugin.utils;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.uast.UastNode;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PluginApiUtilsTest {

  @Test
  void newRange_should_call_newRange_on_inputFile() {
    String value = "dummy value";
    int line = 7;
    int column = 13;

    UastNode.Token token = new UastNode.Token(line, column, value);
    InputFile inputFile = mock(InputFile.class);
    PluginApiUtils.newRange(inputFile, token);

    verify(inputFile).newRange(eq(line), eq(column - 1), eq(line), eq(column - 1 + value.length()));
  }
}
