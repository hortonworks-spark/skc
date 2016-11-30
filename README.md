# spark-kafka-0-10-connector

A Kafka 0.10 connector for Spark 1.x Streaming.

This package is ported from [Apache Spark](http://spark.apache.org) kafka-0-10 module,
modified to make it work with Spark 1.x. To see the detailed changes please refer to 
"change.diff" file.

Build
=====

This package is organized using Maven, to build it:

```
mvn clean package
```

By default it is built against Spark 1.6.2, you could also build with other Spark versions by:

```
mvn clean package -Dspark.version=xxx
```

This package doesn't rely on specific version of Spark, you could run it with Spark version
other than the one built.

How to Use
===

Using `--packages` is the convenient way to add this package to your application, you could do like:

```
spark-submit --master yarn \
    --deploy-mode client \
    --packages com.hortonworks:spark-kafka-0-10-connector_2.10:1.0.0 \
    --class YourApp \
    your-application-jar \
    arg ...
```

To make it work, you should make sure this package is published on to some repositories or be existed
in your local repository.

If this package is not published to repository or your Spark application
cannot access external network, you could use uber jar instead, like:

```
spark-submit --master yarn \
    --deploy-mode client \
    --jars  spark-kafka-0-10-connector-assembly_2.10-1.0.0.jar \
    --class YourApp \
    your-application-jar \
    arg ...
```

# License

Apache License, Version 2.0 [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
