package com.github.xgp.util;

import java.util.concurrent.TimeUnit;

public class BackoffFailureHandler<T> implements Reactor.FailureHandler<T> {

  private final BackOff backOff;

  public BackoffFailureHandler(BackOff backOff) {
    this.backOff = backOff;
  }

  protected boolean canRetry(Throwable t, T e) {
    return true;
  }

  @Override
  public void onFailure(Throwable t, Reactor r, T e) {
    if (!canRetry(t, e)) return;
    long backOffTime = backOff.nextBackOffMillis();
    if (backOffTime == BackOff.STOP) {
      return;
    }
    r.schedule(e, backOffTime, TimeUnit.MILLISECONDS, this);
  }
}
