java_library(
    name = "lib",
    sources = ["**/*.java"],
    dependencies = [
        "3rdparty/jvm/com/google/cloud:google-cloud-pubsub",
        "3rdparty/jvm/commons-cli",
        "3rdparty/jvm/org/slf4j:slf4j-api",
        "finagle/finagle-stats/src/main/scala",
    ],
)

jvm_binary(
    name = "pubsub_subscriper_app",
    main = "com.twitter.pubsub_subscriper_app.SubscriberMain",
    source = "SubscriberMain.java",
    dependencies = [
        ":lib",
        "3rdparty/jvm/com/google/cloud:google-cloud-core",
        "3rdparty/jvm/commons-cli",
        "3rdparty/jvm/org/apache/logging/log4j:log4j-slf4j-impl",
    ],
)
