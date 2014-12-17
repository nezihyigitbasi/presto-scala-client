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
