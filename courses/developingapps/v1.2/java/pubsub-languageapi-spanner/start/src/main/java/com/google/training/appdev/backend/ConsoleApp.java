/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.training.appdev.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;



import com.google.training.appdev.services.gcp.domain.Feedback;
import com.google.training.appdev.services.gcp.languageapi.LanguageService;
import com.google.training.appdev.services.gcp.spanner.SpannerService;

public class ConsoleApp {

  public static void main(String... args) throws Exception {


    String projectId = System.getenv("GCLOUD_PROJECT");
    System.out.println("Project: " + projectId);


    // Notice that the code to create the topic is the same as in the publisher
    TopicName topic = TopicName.create(projectId, "feedback");

    // TODO: Create the languageService

      LanguageService languageService = LanguageService.create();

    // END TODO

    // TODO: Create the spannerService

      SpannerService spannerService = SpannerService.create();

      // END TODO

    // TODO: Create the Pub/Sub subscription name

      SubscriptionName subscription = SubscriptionName.create(projectId, "worker3-subscription");

    // END TODO
    
    // TODO: Create the subscriptionAdminClient

      try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {

          // TODO: create the Pub/Sub subscription using the subscription name and topic

          subscriptionAdminClient.createSubscription(
                  subscription, topic,
                  PushConfig.getDefaultInstance(), 0);

          // END TODO

      }

    // END TODO

    // The message receiver processes Pub/Sub subscription messages
      // Override the receiveMessage(...) method
      MessageReceiver receiver = (message, consumer) -> {
          // TODO: Extract the message data as a JSON String

          String fb = message.getData().toStringUtf8();

          // END TODO

          // TODO: Ack the message

          consumer.ack();

          // END TODO

          try {
              // Object mapper deserializes the JSON String
              ObjectMapper mapper = new ObjectMapper();

              // TODO: Deserialize the JSON String representing the feedback



              // END TODO

              // TODO: Use the Natural Language API to analyze sentiment

              Feedback feedback = mapper.readValue(fb, Feedback.class);
              System.out.println("Feedback received: " + feedback);

              // END TODO

              // TODO: Set the feedback object sentiment score

              float sentimentScore = languageService.analyzeSentiment(feedback.getFeedback());
              feedback.setSentimentScore(sentimentScore);
              System.out.println("Score is: " + sentimentScore);

              // END TODO

              // TODO: Insert the feedback into Cloud Spanner

              spannerService.insertFeedback(feedback);
              System.out.println("Feedback saved: " + feedback);

              // END TODO

          } catch (Exception e) {
              e.printStackTrace();
          }
      };

    // TODO: Declare a subscriber

    Subscriber subscriber = null;

    // END TODO

    try {

        // TODO: Initialize the subscriber using its default builder
        // with a subscription and receiver

        subscriber = Subscriber.defaultBuilder(subscription, receiver).build();

        // END TODO

        // TODO: Add a listener to the subscriber


        subscriber.addListener(
                new Subscriber.Listener() {
                    @Override
                    public void failed(Subscriber.State from, Throwable failure) {
                        System.err.println(failure);
                    }
                },
                MoreExecutors.directExecutor());




        // END TODO

        // TODO: Start subscribing

        subscriber.startAsync().awaitRunning();
        

        // END TODO

        System.out.println("Started. Press any key to quit and remove subscription");

        System.in.read();

    } finally {
        
        // TODO: Stop subscribing

        if (subscriber != null) {
            subscriber.stopAsync().awaitTerminated();
        }

        // END TODO
        

        // TODO: Delete the subscription

        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            subscriptionAdminClient.deleteSubscription(subscription);
        }
        



        // END TODO
    }
    }

}

