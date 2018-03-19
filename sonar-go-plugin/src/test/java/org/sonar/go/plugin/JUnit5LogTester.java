package org.sonar.go.plugin;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.sonar.api.utils.log.LogTester;

/**
 * JUnit 5 extension model has changed. This is to adapt {@link LogTester} to Junit 5 API.
 * Note that we have to depend on {@code junit-jupiter-migrationsupport} to have JUnit 4 dependencies available.
 * Eventually this should be moved to sonar-plugin-api
 */
public class JUnit5LogTester extends LogTester implements BeforeAllCallback, AfterAllCallback {

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    after();
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    try {
      before();
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
  }
}
