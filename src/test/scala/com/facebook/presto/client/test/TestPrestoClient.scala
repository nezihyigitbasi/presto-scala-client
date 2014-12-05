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

  "PrestoClient" should "create the base statement URI correctly" in {
    val client = new PrestoClient(testClientConfig)
    assert(client.statementURL.toString == testURI + "/v1/statement")
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
