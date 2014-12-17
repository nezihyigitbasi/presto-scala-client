/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.client.test

import com.facebook.presto.client.scala.{PrestoClient, PrestoClientConfig}
import org.scalatest._
import scala.collection.JavaConverters._

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 */
class TpchTests extends FlatSpec {

  val testURI = "http://localhost:8080"
  val testSource = "ClientTests"
  val testSchema = "tiny"
  val testCatalog = "tpch"
  val threadCount = 25

  val testClientConfig = new PrestoClientConfig(source = testSource, schema = testSchema, catalog = testCatalog, baseURI = testURI, httpThreadCount = threadCount)
  val prestoClient = new PrestoClient(testClientConfig)

  "The first 3 nations" should "be equal" in {
    val expected = List("ALGERIA", "ARGENTINA", "BRAZIL")
    var index = 0
    prestoClient.submitQuery("select name from nation order by name asc limit 3").filter {
      row => row.getData != null
    }.foreach {
      row => {
        row.getData.asScala.foreach {
          element => {
            assert(element.get(0) == expected(index))
            index += 1
          }
        }
      }
    }
  }

  "The first 5 regions" should "be equal" in {
    val expected = List("AFRICA", "AMERICA", "ASIA", "EUROPE", "MIDDLE EAST")

    prestoClient.submitQuery("select regionkey, name from region order by name asc limit 5").filter {
      row => row.getData != null
    }.foreach {
      row => {
        row.getData.asScala.foreach {
          element => {
            assert(element.get(1) == expected(element.get(0).asInstanceOf[Long].toInt))
          }
        }
      }
    }
  }

  "The total number of available quantities for part 751" should "be 26087" in {
    prestoClient.submitQuery("select partkey, SUM(availqty) from partsupp where partkey=751  group by partkey").filter {
      row => row.getData != null
    }.foreach {
      row => {
        row.getData.asScala.foreach {
          element => {
            assert(element.get(0) == 751)
            assert(element.get(1) == 26087)
          }
        }
      }
    }
  }

  "The total number of available quantities for part 999999" should "return no rows" in {
    var nRows = 0
    prestoClient.submitQuery("select partkey, SUM(availqty) from partsupp where partkey=999999  group by partkey").filter {
      row => row.getData != null
    }.foreach {
      row => {
        row.getData.asScala.foreach {
          element => {
            nRows += 1
          }
        }
      }
    }
    assert(nRows == 0)
  }

  "The distinct ship modes" should "be equal" in {
    val expected = List("AIR", "FOB", "MAIL", "RAIL", "REG AIR", "SHIP", "TRUCK")
    var index = 0

    prestoClient.submitQuery("select distinct(shipmode) from lineitem order by 1 asc").filter {
      row => row.getData != null
    }.foreach {
      row => {
        row.getData.asScala.foreach {
          element => {
            assert(element.get(0) == expected(index))
            index += 1
          }
        }
      }
    }
  }
}
