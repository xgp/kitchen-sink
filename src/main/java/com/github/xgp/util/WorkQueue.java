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
 * Simple task runner that takes items from a queue and hands them off to threads for processing.
 * Restricts concurrency permits with a semaphore.
 */
public class WorkQueue<T> implements Managed, Runnable {

  protected final BlockingQueue<T> queue;
  protected final Function<T, ? extends Object> function;
  protected final Semaphore available;
  protected final ExecutorService executor;
  protected final Set<T> processing;
  protected final long delay;
  protected final int permits;
  protected final Thread thread;

  public WorkQueue(Function<T, ? extends Object> function) {
    this(
        new LinkedBlockingQueue<T>(),
        function,
        Runtime.getRuntime().availableProcessors(),
        Runtime.getRuntime().availableProcessors(),
        1000l);
  }

  public WorkQueue(
      BlockingQueue<T> queue,
      Function<T, ? extends Object> function,
      int permits,
      int threads,
      long delay) {
    if (permits > threads) throw new IllegalStateException("permits must be <= threads");
    this.permits = permits;
    this.delay = delay;
    this.queue = queue;
    this.function = function;
    this.available = new Semaphore(permits, true);
    this.executor = Executors.newFixedThreadPool(threads);
    this.processing = Collections.newSetFromMap(new WeakHashMap<T, Boolean>());
    this.thread = new Thread(this);
  }

  /** @return the Queue. */
  public BlockingQueue<T> getQueue() {
    return queue;
  }

  /** @return the Function handler. */
  public Function<T, ? extends Object> getFunction() {
    return function;
  }

  public Set<T> getProcessing() {
    return processing;
  }

  private volatile boolean running = false;

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() throws Exception {
    try {
      thread.start();
      Managed.addShutdownHook(this);
    } catch (IllegalThreadStateException e) {
      throw new Exception(e);
    }
  }

  @Override
  public void stop() {
    running = false;
    executor.shutdown();
  }

  @Override
  public void await() {
    try {
      executor.awaitTermination(delay * permits, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
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
