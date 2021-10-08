package com.github.xgp.util;

/** A managed service. */
public interface Managed {

  /** @return true if this service is running. */
  boolean isRunning();

  /**
   * Initiates service startup.
   *
   * @throws IllegalStateException if the service is already running.
   */
  void start() throws Exception;

  /** Stops the service. */
  void stop();

  /**
   * Waits for the service to complete stopping. Returns immediately if the service has not yet been
   * started or is already stopped.
   */
  void await();

  /**
   * Registers a shutdown hook for this service.
   *
   * @param managed The service.
   */
  public static void addShutdownHook(final Managed managed) {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                managed.stop();
                managed.await();
              }
            });
  }
}
