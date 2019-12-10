name := "Spark_Streaming_HBase"

version := "1.0"

scalaVersion := "2.11.0"

val sparkVer = "2.4.4"

libraryDependencies ++=
  Seq(
    "org.apache.spark" %% "spark-core" % sparkVer,
    "org.apache.spark" %% "spark-streaming" % sparkVer,
    "org.apache.kafka" % "kafka-clients" % "0.8.2.0",
    "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % sparkVer,
    "org.apache.hbase" % "hbase" % "1.2.0" pomOnly(),
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
    "org.apache.hbase" % "hbase-server" % "1.2.0",
    "org.apache.hbase" % "hbase-client" % "1.2.0",
    "org.apache.hbase" % "hbase-common" % "1.2.0",
    "org.apache.hbase" % "hbase-mapreduce" % "2.1.4",
    "org.apache.hbase" % "hbase-protocol" % "1.2.0",
    "org.apache.commons" % "commons-configuration2" % "2.4"
  )