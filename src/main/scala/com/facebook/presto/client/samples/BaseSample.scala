package com.facebook.presto.client.samples

import com.facebook.presto.client.scala.{PrestoClientConfig, PrestoClient}

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 */
trait BaseSample {

  def createPrestoClient(args: Array[String]): PrestoClient = {
    require(args.length == 3, "Needs the SQL query, the schema, and the catalog arguments")
    new PrestoClient(new PrestoClientConfig(schema=args(1), catalog=args(2)))
  }
}
