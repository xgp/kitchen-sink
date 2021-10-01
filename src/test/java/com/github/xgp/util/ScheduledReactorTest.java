package com.github.xgp.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Ignore;
import org.junit.Test;

public class ScheduledReactorTest {

  @Test
  public void runOneTask() throws Exception {
    final AtomicInteger count = new AtomicInteger(0);
    Reactor<String> reactor = new ScheduledReactor(s -> count.getAndIncrement(), 1);
    reactor.schedule("foo");
    reactor.stop();
    Thread.sleep(500L);
    assertThat(count.get(), is(1));
  }

  @Ignore
  @Test
  public void runOneTaskWithBackoff() throws Exception {
    final AtomicInteger count = new AtomicInteger(0);
    ExponentialBackOff backoff =
        new ExponentialBackOff.Builder()
            .setInitialIntervalMillis(500)
            .setMaxElapsedTimeMillis(9000)
            .setMaxIntervalMillis(6000)
            .setMultiplier(1.5)
            .setRandomizationFactor(0.5)
            .build();
    BackoffFailureHandler<String> handler =
        new BackoffFailureHandler<String>(backoff) {
          @Override
          protected boolean canRetry(Throwable t, String e) {
            count.getAndIncrement();
            return true;
          }
        };
    Reactor<String> reactor =
        new ScheduledReactor(
            s -> {
              System.err.println(s);
              throw new RuntimeException();
            },
            1);
    reactor.schedule("foo", handler);
    Thread.sleep(10000L);
    reactor.stop();
    Thread.sleep(10000L);
    assertThat(count.get(), is(1));
  }
}
