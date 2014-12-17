package com.facebook.presto.client.samples

import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 */
object QueryStatisticsSample extends BaseSample with LazyLogging {

  def main(args: Array[String]) {
    val client = createPrestoClient(args)
    val query = args(0)
    val schema = args(1)
    val catalog = args(2)
    logger.info(s"Will run query=${query} on schema=${schema} catalog=${catalog}")

    val cursor = client.submitQuery(query)
    cursor.foreach {
      queryResult => println(queryResult)
    }

    println("Query statistics:")
    client.getQueryStatistics(cursor.queryId.get.get, statistics =>
      for ((key,value) <- statistics)
        println(s"${key} => ${value}")
    )
  }
}
