package com.facebook.presto.client.scala

import scala.concurrent.duration._

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 * Configuration used when constructing a [[PrestoClient]]
 */
class PrestoClientConfig(val source: String = "presto-scala-client", val schema: String = "default",
                         val catalog: String = "default", val baseURI: String = "http://localhost:8080",
                         val timeout: Duration = 10 seconds, val httpThreadCount: Int = 20)
