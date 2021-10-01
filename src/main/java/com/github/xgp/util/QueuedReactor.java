package com.github.xgp.util;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Simple reactor that takes items from a queue and hands them off to threads for processing.
 * Restricts concurrency permits with a semaphore.
 */
public class QueuedReactor<T> implements Reactor<T> {

  protected final BlockingQueue<Task<T>> queue;
  protected final Function<T, ? extends Object> function;
  protected final Semaphore available;
  protected final ExecutorService executor;
  protected final Set<T> processing;
  protected final long delay;
  ;

  public QueuedReactor(
      Function<T, ? extends Object> function, int permits, int threads, long delay) {
    if (permits > threads) throw new IllegalStateException("permits must be <= threads");
    this.delay = delay;
    this.function = function;
    this.available = new Semaphore(permits, true);
    this.queue = new LinkedBlockingQueue<Task<T>>();
    this.executor = Executors.newFixedThreadPool(threads);
    this.processing = Collections.newSetFromMap(new WeakHashMap<T, Boolean>());
  }

  /** @return the Function handler used by this reactor. */
  public Function<T, ? extends Object> getFunction() {
    return this.function;
  }

  /** @return the Executor used by this reactor. */
  public ExecutorService getExecutor() {
    return this.executor;
  }

  @Override
  public Set<T> getProcessing() {
    return this.processing;
  }

  private volatile boolean running = false;

  @Override
  public boolean isRunning() {
    return this.running;
  }

  @Override
  public void stop() {
    this.running = false;
  }

  @Override
  public void schedule(T e) {
    schedule(e, null);
  }

  @Override
  public void schedule(T e, FailureHandler<T> handler) {
    try {
      queue.put(new Task<T>(e, handler));
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  @SuppressWarnings("SleepWhileInLoop")
  public void run() {
    running = true;
    final Reactor reactor = this;
    while (isRunning() && !Thread.currentThread().isInterrupted()) {
      try {
        if (available.tryAcquire(delay, TimeUnit.MILLISECONDS)) {
          try {
            final Task<T> task = queue.poll(delay, TimeUnit.MILLISECONDS);
            if (task == null) {
              available.release();
            } else {
              executor.submit(
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
                        available.release();
                      }
                    }
                  });
            }
          } catch (Exception e) {
            available.release();
            Thread.sleep(delay);
          }
        }
      } catch (InterruptedException e) {
        stop();
      }
    }
  }
}
