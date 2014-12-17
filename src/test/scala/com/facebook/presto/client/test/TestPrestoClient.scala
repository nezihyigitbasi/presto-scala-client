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
import com.facebook.presto.client.PrestoHeaders._

import org.scalatest._

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 */
class TestPrestoClient extends FlatSpec {

  val testURI = "http://host:9999"
  val testSource = "ClientTests"
  val testSchema = "test_schema"
  val testCatalog = "test_catalog"
  val threadCount = 99

  val testClientConfig = new PrestoClientConfig(source = testSource, schema = testSchema, catalog = testCatalog, baseURI = testURI, httpThreadCount = threadCount)

  "PrestoClient" should "initialize URLs correctly" in {
    val client = new PrestoClient(testClientConfig)
    assert(client.statementURL.toString == testURI + "/v1/statement")
    assert(client.queryURL.toString == testURI + "/v1/query")
  }

  "PrestoClient" should "create the HTTP headers correctly" in {
    val client = new PrestoClient(testClientConfig)
    val sourceHeader = client.httpHeaders.find(x => x._1 == PRESTO_SOURCE)
    val userHeader = client.httpHeaders.find(x => x._1 == PRESTO_USER)
    val schemaHeader = client.httpHeaders.find(x => x._1 == PRESTO_SCHEMA)
    val catalogHeader = client.httpHeaders.find(x => x._1 == PRESTO_CATALOG)

    assert(sourceHeader.isDefined)
    assert(userHeader.isDefined)
    assert(schemaHeader.isDefined)
    assert(catalogHeader.isDefined)
    assert(sourceHeader.get._2 == testSource)
    assert(userHeader.get._2 == System.getProperty("user.name"))
    assert(schemaHeader.get._2 == testSchema)
    assert(catalogHeader.get._2 == testCatalog)
  }

  "PrestoClient" should "throw an IllegalArgumentException for null queries" in {
    val client = new PrestoClient(testClientConfig)

    intercept[IllegalArgumentException] {
      client.submitQuery(null)
    }

    intercept[IllegalArgumentException] {
      client.submitQuery(null, null)
    }
  }
}
