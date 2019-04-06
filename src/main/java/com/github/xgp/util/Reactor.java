package com.github.xgp.util;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Simple reactor that takes items from a queue and hands them off to threads for processing.
 * Restricts concurrency permits with a semaphore.
 */
public class Reactor<T> implements Runnable {

  protected final BlockingQueue<T> queue;
  protected final Function<T, ? extends Object> function;
  protected final Semaphore available;
  protected final ExecutorService executor;
  protected final Set<T> processing;
  protected final long delay;;

  public Reactor(
      BlockingQueue<T> queue,
      Function<T, ? extends Object> function,
      int permits,
      int threads,
      long delay) {
    if (permits > threads) throw new IllegalStateException("permits must be <= threads");
    this.delay = delay;
    this.queue = queue;
    this.function = function;
    this.available = new Semaphore(permits, true);
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

  /** @return Set of items currently being processed. */
  public Set<T> getProcessing() {
    return this.processing;
  }

  private volatile boolean running = false;

  /** @return true if the reactor is running in a thread. */
  public boolean isRunning() {
    return this.running;
  }

  /** Initiates an orderly shutdown of the reactor. */
  public void stop() {
    this.running = false;
  }

  @Override
  @SuppressWarnings("SleepWhileInLoop")
  public void run() {
    running = true;
    while (isRunning() && !Thread.currentThread().isInterrupted()) {
      try {
        if (available.tryAcquire(delay, TimeUnit.MILLISECONDS)) {
          try {
            final T item = queue.poll(delay, TimeUnit.MILLISECONDS);
            if (item == null) {
              available.release();
            } else {
              executor.submit(
                  new Runnable() {
                    public void run() {
                      try {
                        processing.add(item);
                        function.apply(item);
                      } finally {
                        processing.remove(item);
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
