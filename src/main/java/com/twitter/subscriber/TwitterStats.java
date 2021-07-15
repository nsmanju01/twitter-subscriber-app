package com.twitter.subscriber;


import com.twitter.finagle.stats.DefaultStatsReceiver;
import com.twitter.finagle.stats.StatsReceiver;

public class TwitterStats {

  private final StatsReceiver statsReceiver;

  public TwitterStats() {
    this.statsReceiver = DefaultStatsReceiver.get().scope("subscriber-app");
  }

  public void recordConsumeLatencyStat(Long latency) {
    statsReceiver.stat("streaming_subscriber_latency_millis").add(latency);
  }
}
