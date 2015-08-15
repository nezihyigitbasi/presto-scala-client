package com.facebook.presto.client.scala

import io.airlift.units.Duration
import org.joda.time.DateTime

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 *
 */
case class PrestoNode(uri: String, recentRequests: Double, recentFailures: Double,
                  recentSuccesses: Double, lastRequestTime: DateTime,
                  lastResponseTime: DateTime, recentFailureRatio: Double,
                   age: Duration, recentFailuresByType: Map[String, Double]) {
}
