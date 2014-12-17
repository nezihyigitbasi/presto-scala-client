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
