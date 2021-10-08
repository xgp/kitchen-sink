package com.github.xgp.util;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Reactor takes items from a source and hands them off to threads for processing. */
public interface Reactor<T> {
  /**
   * Schedule the element for immediate execution.
   *
   * @param e the task to be scheduled
   */
  void schedule(T e);

  /**
   * Schedule the element for immediate execution.
   *
   * @param e the task to be scheduled
   * @param handler what to do if there is a failure
   */
  void schedule(T e, FailureHandler<T> handler);

  /**
   * Schedule the element for delayed execution.
   *
   * @param e the task to be scheduled
   * @param delay the amount of the delay
   * @param unit the time unit of the delay amount
   */
  default void schedule(T e, long delay, TimeUnit unit) {
    throw new UnsupportedOperationException("Delayed scheduling is not supported by this Reactor");
  }

  /**
   * Schedule the element for delayed execution.
   *
   * @param e the task to be scheduled
   * @param delay the amount of the delay
   * @param unit the time unit of the delay amount
   * @param handler what to do if there is a failure
   */
  default void schedule(T e, long delay, TimeUnit unit, FailureHandler<T> handler) {
    throw new UnsupportedOperationException("Delayed scheduling is not supported by this Reactor");
  }

  /** Initiates an orderly shutdown of the reactor. */
  public void stop();

  /** @return Set of items currently being processed. */
  Set<T> getProcessing();

  interface FailureHandler<T> {
    void onFailure(Throwable t, Reactor r, T e);

    FailureHandler DO_NOTHING =
        new FailureHandler() {
          @Override
          public void onFailure(Throwable t, Reactor r, Object e) {}
        };
  }

  class Task<T> {
    private final T task;
    private final Reactor.FailureHandler<T> handler;

    public Task(T task, Reactor.FailureHandler<T> handler) {
      this.task = task;
      this.handler = handler;
    }

    public T getTask() {
      return this.task;
    }

    public Reactor.FailureHandler<T> getHandler() {
      return this.handler;
    }
  }
}
