package com.github.xgp.util;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/** Reactor that uses a ScheduledExecutorService for delay scheduling and processing. */
public class ScheduledReactor<T> implements Reactor<T> {

  protected final ScheduledExecutorService pending;
  protected final Function<T, ? extends Object> function;
  protected final Set<T> processing;

  public ScheduledReactor(Function<T, ? extends Object> function, int threads) {
    this.function = function;
    this.processing = Collections.newSetFromMap(new WeakHashMap<T, Boolean>());
    this.pending = Executors.newScheduledThreadPool(threads);
  }

  @Override
  public Set<T> getProcessing() {
    return this.processing;
  }

  private volatile boolean running = false;

  @Override
  public boolean isRunning() {
    return !pending.isShutdown();
  }

  @Override
  public void stop() {
    pending.shutdown();
  }

  @Override
  public void schedule(T e) {
    schedule(e, 0, TimeUnit.MILLISECONDS);
  }

  @Override
  public void schedule(T e, FailureHandler<T> handler) {
    schedule(e, 0, TimeUnit.MILLISECONDS, handler);
  }

  @Override
  public void schedule(T e, long delay, TimeUnit unit) {
    schedule(e, delay, unit, null);
  }

  @Override
  public void schedule(T e, long delay, TimeUnit unit, FailureHandler<T> handler) {
    schedule(new Task<T>(e, handler), delay, unit);
  }

  void schedule(Task<T> task, long delay, TimeUnit unit) {
    final Reactor reactor = this;
    pending.schedule(
        new Runnable() {
          public void run() {
            try {
              processing.add(task.getTask());
              function.apply(task.getTask());
            } catch (Throwable t) {
              if (task.getHandler() != null) {
                try {
                  task.getHandler().onFailure(t, reactor, task.getTask());
                } catch (Exception e) {
                }
              }
            } finally {
              processing.remove(task.getTask());
            }
          }
        },
        delay,
        unit);
  }

  /*
  private void schedule(final RetryableEvent event) {
      if (!enabled) return;
      if (event.getTries() > MAX_RETRIES) {
        deadLetter(event);
        return;
      }
      long delay = 0;
      // TODO this is dumb right now. not detection of "back up" that clears the delay queue.
      if (event.getTries() > 0) delay = (long) Math.pow(2, event.getTries());

      log.debugf("Schedule event with delay of %ss", delay);
      pendingEvents.schedule(
          () -> {
            try {
              sendJson(toJson(event.getEvent()));
            } catch (EventEmitterException | IOException e) {
              log.debugf("Failed to serialize or send event: %s", e.getMessage());
              if (e instanceof EventEmitterException) {
                if (!((EventEmitterException) e).canRetry()) {
                  deadLetter(event);
                  return;
                }
              }
              event.incrementTries();
              schedule(event);
            }
          },
          delay,
          TimeUnit.SECONDS);
    }
          */
}
