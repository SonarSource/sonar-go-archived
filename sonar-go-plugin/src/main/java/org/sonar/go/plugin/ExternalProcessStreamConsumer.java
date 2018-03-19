package org.sonar.go.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


class ExternalProcessStreamConsumer {

  private static final Logger LOG = Loggers.get(ExternalProcessStreamConsumer.class);
  private ExecutorService executorService;

  public ExternalProcessStreamConsumer() {
    executorService = Executors.newCachedThreadPool(r -> {
      Thread thread = new Thread(r);
      thread.setName("stream-consumer");
      thread.setDaemon(true);
      return thread;
    });
  }

  public final void consumeStream(InputStream inputStream, StreamConsumer streamConsumer) {
    executorService.submit(() -> {
      try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        readErrors(errorReader, streamConsumer);
      } catch (IOException e) {
        LOG.error("Error while reading stream", e);
      }
    });
  }

  protected void readErrors(BufferedReader errorReader, StreamConsumer streamConsumer) {
    errorReader.lines().forEach(streamConsumer::consumeLine);
    streamConsumer.finished();
  }

  interface StreamConsumer {

    void consumeLine(String line);

    default void finished() {

    }
  }
}
