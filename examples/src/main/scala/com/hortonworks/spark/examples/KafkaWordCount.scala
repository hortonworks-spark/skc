/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hortonworks.spark.examples

import java.io.FileInputStream
import java.util.Properties

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.control.NonFatal

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

/**
 * How to use:
 *
 * spark-submit --driver-java-options="-Djava.security.auth.login.config=<path-to>/kafka_client_jaas.conf" \
 * --master local[*] \
 * --class com.hortonworks.spark.examples.KafkaWordCount \
 * --jars spark-kafka-0-10-connector-assembly_2.10-1.0.0.jar \
 * spark-kafka-0-10-examples-1.0.0.jar <broker-list> <group-id> <topic-list> <additional-kafka-params>
 *
 * If you want to run this example under security Kafka, you should specified
 * kafka_client_jaas.conf in both driver and executor side. Also make sure this config file is
 * reachable in both driver and executor side.
 *
 * Also you should add additional consumer configurations in <additional-kafka-params> file, like:
 *
 * security.protocol=SASL_PLAINTEXT
 * sasl.mechanism=GSSAPI
 * sasl.kerberos.service.name=kafka
 */
object KafkaWordCount {

  def main(args: Array[String]): Unit = {
    if (args.length != 4) {
      System.err.println(
        "KafkaWordCount: bootstrap_server_list group_id topic_list consumer.cfg")
      System.exit(-1)
    }

    val Array(brokers, groupId, topics, props) = args
    val kafkaParams = new mutable.HashMap[String, Object]()
    kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])

    var in: FileInputStream = null
    try {
      val properties = new Properties()
      in = new FileInputStream(props)
      properties.load(in)
      properties.asScala.foreach { case (k, v) =>
        if (!kafkaParams.contains(k)) {
          kafkaParams.put(k, v)
        }
      }
    } catch {
      case NonFatal(e) => System.err.println(e)
    } finally {
      if (in != null) {
        in.close()
        in = null
      }
    }

    val topicSet = topics.split(",").map(_.trim).filter(!_.isEmpty).toSet

    val consumerStrategy = ConsumerStrategies.Subscribe[String, String](topicSet, kafkaParams)

    val sparkConf = new SparkConf()
    val ssc = new StreamingContext(sparkConf, Duration(2000L))

    val stream = KafkaUtils.createDirectStream(
      ssc, LocationStrategies.PreferBrokers, consumerStrategy)

    val words = stream.flatMap { r => r.value().split(" ") }.map { r => (r, 1) }.reduceByKey(_ + _)

    words.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
