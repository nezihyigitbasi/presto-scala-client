package com.facebook.presto.client.samples

import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.JavaConverters._

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 */
object CallbackSample extends BaseSample with LazyLogging {
  def main(args: Array[String]): Unit = {
    val client = createPrestoClient(args)
    val query = args(0)
    val schema = args(1)
    val catalog = args(2)

    logger.info(s"Will run query=${query} on schema=${schema} catalog=${catalog}")

    client.submitQuery(query,
      queryResult => {
        if (queryResult.getData != null) logger.info(queryResult.getData.asScala.mkString("--"))
      })
  }
}
