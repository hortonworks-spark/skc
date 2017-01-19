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

Running on a Kerberos-Enabled Cluster
===

Basically you should follow the [Kafka docs](https://kafka.apache.org/documentation/#security_sasl)
to Make Kafka Brokers SASL-ed/Kerberized. The following instructions assume that Spark and Kafka
are already deployed on a Kerberos-enabled cluster.

* Generate a keytab for the login user and principal you want to run Spark Streaming application.
* Create a jaas configuration file (for example, key.conf), and add configuration settings to specify the user keytab.

    The following example specifies keytab location ./v.keytab for user vagrant@example.com.

    The keytab and configuration files will be distributed using YARN local resources. They will end up
    in the current directory of the Spark YARN container, thus the location should be specified as
    ./v.keytab.

    ```
        KafkaClient {
            com.sun.security.auth.module.Krb5LoginModule required
            useKeyTab=true
            keyTab="./v.keytab"
            storeKey=true
            useTicketCache=false
            serviceName="kafka"
            principal="vagrant@EXAMPLE.COM";
        };
    ```

* In your job submission instructions, pass the jaas configuration file and keytab as local resource files.
Add the jaas configuration file options to the JVM options specified for the driver and executor

    ```
        --files key.conf#key.conf,v.keytab#v.keytab
        --driver-java-options "-Djava.security.auth.login.config=./key.conf"
        --conf "spark.executor.extraJavaOptions=-Djava.security.auth.login.config=./key.conf"
    ```

* Pass any relevant Kafka security options to your streaming application.

# License

Apache License, Version 2.0 [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
