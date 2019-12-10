package com.SparkStreamingHBase2

import org.apache.hadoop.hbase.{HBaseConfiguration, HTableDescriptor}
import org.apache.hadoop.hbase.client.{HTable, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._

object SparkStreamingHBase_2 {

  // Set logs to display ERROR only
  StreamingExamples.setStreamingLogLevels()

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("HBaseTest")
    val ssc = new StreamingContext(conf, Seconds(2))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092", //not zookeeper only broker server
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "stream_consumer_group_id",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array("trump")
    val kafkaStream = KafkaUtils.createDirectStream[String,String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    val raw_text = kafkaStream.map(x => scala.util.parsing.json.JSON.parseFull(x.value())
      .get.asInstanceOf[Map[String, Any]]).filter(x => x.get("lang").mkString == "en")

    ///create row with 2 field
    val tweet = raw_text.map(x => (x.get("id").mkString, x.get("text").mkString))

    //change structure to list for send to dataframe
    val new_line = tweet.map(x => (x._1, x._2))

    //Hbase
    ssc.checkpoint("hdfs://localhost:9000/kafkaHBase")

    def toHBase(row: (_,_)): Unit ={
      val hConf = new HBaseConfiguration()
      hConf.set("hbase.zookeeper.quorum", "localhost:2181")
      val tableName = "twitter"
      val hTable = new HTable(hConf, tableName)
      val tableDescriptor = new HTableDescriptor(tableName)
      val thePut = new Put(Bytes.toBytes(row._1.toString))
      thePut.addColumn(Bytes.toBytes("tweets"), Bytes.toBytes("id"), Bytes.toBytes(row._1.toString))
      thePut.addColumn(Bytes.toBytes("tweets"), Bytes.toBytes("text"), Bytes.toBytes(row._2.toString))
      hTable.put(thePut)
    }

    val HBase_insert: Unit = new_line.foreachRDD(rdd => if (!rdd.isEmpty()) rdd.foreach(toHBase(_)))
    ssc.start()
    ssc.awaitTermination()
  }

}
