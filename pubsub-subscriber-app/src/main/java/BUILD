java_library(
    name = "lib",
    sources = ["**/*.java"],
    dependencies = [
        "3rdparty/jvm/com/google/cloud:google-cloud-pubsub",
        "3rdparty/jvm/commons-cli",
        "3rdparty/jvm/org/slf4j:slf4j-api",
        "finagle/finagle-stats",
        "twitter-server/server/src/main/scala",
    ],
)

jvm_binary(
    name = "pubsub_subscriper_app",
    main = "com.twitter.subscriber.Subscriber$Main",
    source = "SubscriberMain.java",
    dependencies = [
        ":lib",
        "3rdparty/jvm/org/slf4j:jcl-over-slf4j",
        "3rdparty/jvm/org/slf4j:log4j-over-slf4j",
        "finatra/inject/inject-logback/src/main/scala",
        "kafka/pubsub-subscriber-app/src/main/resources",
        "twitter-server/logback-classic/src/main/scala",
    ],
)
