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

import scala.collection.mutable
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

object KafkaWordCount {

  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      System.err.println(s"KafkaWordCount: bootstrap_server_list group_id topic_list")
      System.exit(-1)
    }

    val Array(brokers, groupId, topics) = args
    val kafkaParams = new mutable.HashMap[String, Object]()
    kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])

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
