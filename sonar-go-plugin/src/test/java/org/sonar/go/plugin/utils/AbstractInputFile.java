package org.sonar.go.plugin.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import javax.annotation.CheckForNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;

/**
 * A dumb implementation (generated with IDE) to make it easy in tests
 * to override just the methods needed for capturing method calls.
 * (See for example CpdVisitorTest.)
 */
public class AbstractInputFile implements InputFile {
  @Override
  public String relativePath() {
    return null;
  }

  @Override
  public String absolutePath() {
    return null;
  }

  @Override
  public File file() {
    return null;
  }

  @Override
  public Path path() {
    return null;
  }

  @Override
  public URI uri() {
    return null;
  }

  @Override
  public String filename() {
    return null;
  }

  @CheckForNull
  @Override
  public String language() {
    return null;
  }

  @Override
  public Type type() {
    return null;
  }

  @Override
  public InputStream inputStream() throws IOException {
    return null;
  }

  @Override
  public String contents() throws IOException {
    return null;
  }

  @Override
  public Status status() {
    return null;
  }

  @Override
  public int lines() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public TextPointer newPointer(int line, int lineOffset) {
    return null;
  }

  @Override
  public TextRange newRange(TextPointer start, TextPointer end) {
    return null;
  }

  @Override
  public TextRange newRange(int startLine, int startLineOffset, int endLine, int endLineOffset) {
    return null;
  }

  @Override
  public TextRange selectLine(int line) {
    return null;
  }

  @Override
  public Charset charset() {
    return null;
  }

  @Override
  public String key() {
    return null;
  }

  @Override
  public boolean isFile() {
    return false;
  }
}
