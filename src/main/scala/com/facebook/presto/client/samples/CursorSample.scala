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
package com.facebook.presto.client.samples

import com.facebook.presto.client.scala.{PrestoClient}
import com.typesafe.scalalogging.slf4j.{LazyLogging}
import scala.collection.JavaConverters._

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 */
object CursorSample extends BaseSample with LazyLogging {

  def printQueryResults(client: PrestoClient, query: String) = {
    //just print the raw data
    client.submitQuery(query).foreach {
      queryResult => println(queryResult)
    }
  }

  def printData(client: PrestoClient, query: String) = {
    //print each list of data
    client.submitQuery(query).filter {
      result => result.getData != null
    }.foreach {
      result => logger.info(result.getData.asScala.mkString("--"))
    }
  }

  def forComprehension(client: PrestoClient, query: String) = {
    //can also use for-comprehensions
    val data = for {
      queryResult <- client.submitQuery(query) if queryResult.getData != null
    } yield queryResult.getData.asScala

    val nRows = data.foldLeft(0)((c,list) => c + list.size)
    logger.info(s"${nRows} rows retrieved")
  }

  def rowCount(client: PrestoClient, query: String) = {
    //another way of getting the number of rows retrieved
    val nRows = client.submitQuery(query).
                       filter(_.getData != null).
                       foldLeft(0)((c,queryResults) => c + queryResults.getData.iterator.asScala.toList.size)
    logger.info(s"${nRows} rows retrieved")
  }

  def main(args: Array[String]) {
    val client = createPrestoClient(args)
    val query = args(0)
    val schema = args(1)
    val catalog = args(2)
    logger.info(s"Will run query=${query} on schema=${schema} catalog=${catalog}")
    printData(client, query)
  }
}
