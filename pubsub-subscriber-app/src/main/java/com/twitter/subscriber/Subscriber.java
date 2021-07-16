package com.twitter.subscriber;

import java.util.concurrent.TimeUnit;

import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.protobuf.util.Timestamps;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import com.twitter.app.Flag;
import com.twitter.app.Flaggable;
import com.twitter.server.AbstractTwitterServer;


public class Subscriber extends AbstractTwitterServer {
  private static final Logger log = LoggerFactory.getLogger(Subscriber.class);
  private static TwitterStats stats = new TwitterStats();


  private Flag<String> projectIdFlag = flag().create(
      "projectId",
      "messaging-infrastructure",
      "project ID",
      Flaggable.ofString());

  private Flag<String> subscriptionIDFlag = flag().create(
      "subscriptionId",
      "",
      "Subscription ID",
      Flaggable.ofString());

  private Flag<Integer> parallelPullCountFlag = flag().create(
      "parallelPullCount",
      4,
      "Parall Pull Count ",
      Flaggable.ofJavaInteger());

  private Flag<Integer> maxAckExtensionPeriod = flag().create(
      "maxAckExtensionPeriod",
      10000,
      "maxAckExtensionPeriod in millis ",
      Flaggable.ofJavaInteger());

  private Flag<Integer> maxDurationPerAckExtension = flag().create(
      "maxDurationPerAckExtension",
      10000,
      "maxDurationPerAckExtension in millis",
      Flaggable.ofJavaInteger());

  @Override
  public void main() {
    subscribeWithConcurrencyControl(
        this.projectIdFlag.apply(),
        this.subscriptionIDFlag.apply(),
        this.parallelPullCountFlag.apply());

    // Prevents main thread from exiting
    try {
      for (; ; ) {
        Thread.sleep(Long.MAX_VALUE);
      }
    } catch (Exception ex) {
      log.error("Exceiption in sleep call", ex);
    }
  }

  public void subscribeWithConcurrencyControl(
      String projectId, String subscriptionId, Integer parallelPullCountVal) {
    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.newBuilder()
            .setSubscription(subscriptionId)
            .setProject(projectId)
            .build();


    // Instantiate an asynchronous message receiver.
    MessageReceiver receiver =
        (PubsubMessage message, AckReplyConsumer consumer) -> {
          // Handle incoming message, then ack the received message.
          long latency = System.currentTimeMillis() - Timestamps.toMillis(message.getPublishTime());
          stats.recordConsumeLatencyStat(latency);
          log.info("message-id {}, latency {}", message.getMessageId(), latency);
          consumer.ack();
        };

    com.google.cloud.pubsub.v1.Subscriber subscriber = null;
    try {

      ExecutorProvider executorProvider =
          InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(5).build();

      // using the custom executor provider uses non-daemon threads which prevents subscriber from exiting
      subscriber =
          com.google.cloud.pubsub.v1.Subscriber.newBuilder(subscriptionName, receiver)
              .setParallelPullCount(parallelPullCountVal)
              .setExecutorProvider(executorProvider)
              .setMaxAckExtensionPeriod(Duration.ofMillis(this.maxAckExtensionPeriod.apply()))
              .setMaxDurationPerAckExtension(Duration.ofMillis(this.maxDurationPerAckExtension.apply()))
              .build();

      // Start the subscriber and wait until it reaches running state
      subscriber.startAsync().awaitRunning();

      log.info("Started Subscriber");
    } catch (Exception e) {
      log.error("Error in subscribeWithConcurrencyControl", e);
      // Shut down the subscriber after 30s. Stop receiving messages.
      if (subscriber != null) {
        if (subscriber.isRunning()) {
          subscriber.stopAsync().awaitTerminated();
        }
      }
    }
  }

  public static class Main {
    public static void main(final String[] args) throws Exception {
      new Subscriber().main(args);
    }
  }
}
