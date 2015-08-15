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
package com.facebook.presto.client.scala

import java.net.{URI, URL}

import com.facebook.presto.client.PrestoHeaders._
import com.facebook.presto.client.QueryResults
import com.google.common.base.Charsets
import com.stackmob.newman.dsl._
import com.stackmob.newman.response.HttpResponse
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.airlift.units.Duration
import net.liftweb.json.JsonAST.{JString, JValue}
import net.liftweb.json._
import org.joda.time.format.ISODateTimeFormat

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.runtime.BoxedUnit
import scala.util.{Failure, Success}


/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 *
 * PrestoClient is used to submit queries to the Presto server.
 * It supports three styles for query processing:
 *  - Using actors: [[com.facebook.presto.client.scala.PrestoActor]]
 *  - Using cursors (for other examples see [[com.facebook.presto.client.samples.CursorSample]])
 *  {{{
    client.submitQuery(query).foreach {
      queryResult => println(queryResult)
    }
 *  }}}
 *  - Using callback functions
 *  {{{
    client.submitQuery(query,
    queryResult => {
      queryResult => println(queryResult)
    })
 *  }}}
 */
class PrestoClient(val config: PrestoClientConfig) extends LazyLogging {
  require(config != null)
  val statementURL = new URL(config.baseURI + "/v1/statement")
  val queryURL = new URL(config.baseURI + "/v1/query")
  val nodeURL = new URL(config.baseURI + "/v1/node")

  val httpHeaders = List(
    (PRESTO_USER, System.getProperty("user.name")),
    (PRESTO_SOURCE, config.source),
    (PRESTO_CATALOG, config.catalog),
    (PRESTO_SCHEMA, config.schema)
  )

  //used by the GET/POST methods
  implicit val httpClient = HttpClientFactory.createApacheHttpClient(config.httpThreadCount)

  /**
   * Submits a query to the Presto server. The caller should use the returned
   * cursor to retrieve the results page by page
   * @param query
   * @return a [[PrestoCursor]]
   */
  def submitQuery(query: String) : PrestoCursor = {
    require(query != null)
    val cursor = new PrestoCursor(query, this)
    cursor.queryInProgress.set(Option(POST(statementURL).setHeaders(httpHeaders).setBody(query).apply))
    cursor
  }

  private [scala] def next(uri: URI): Future[QueryResults] = {
    require(uri != null)
    GET(uri.toURL).setHeaders(httpHeaders).apply.transform(
      response =>
      {
        QueryResultsMapper.getQueryResults(response)
      },
      f => throw f
    )
  }

  private [scala] def delete(queryId: String): Future[HttpResponse] = {
    require(queryId != null)
    DELETE(new URL(queryURL + "/" + queryId)).setHeaders(httpHeaders).apply
  }

  /**
   * Submits a query to the server and calls the given processFunction with the query results
   * @param query
   * @param processFunction
   */
  def submitQuery(query: String, processFunction: QueryResults => Unit) = {
    require(query != null)
    POST(statementURL).setHeaders(httpHeaders).setBody(query).apply.onComplete {
      case Success(response) =>  consumeAll(response, processFunction)
      case Failure(e) => throw e
    }
  }

  private [scala] def consumeAll(response: HttpResponse, processFunction: QueryResults => Unit): Unit = {
    val results = QueryResultsMapper.getQueryResults(response)
    processFunction(results)

    if (results.getNextUri == null) return

    GET(results.getNextUri.toURL).setHeaders(httpHeaders).apply.onComplete {
      case Success(response) =>  consumeAll(response, processFunction) //follow the chain of next URIs
      case Failure(e) => throw e
    }
  }

  /**
   * Retrieves the query statistics from the Presto coordinator and calls the given processFunction with the statistics
   * @param queryId
   * @param callback
   */
  def getQueryStatistics(queryId: String, callback: Map[String, Any] => Unit) = {
    require(queryId != null)
    val url = new URL(s"${queryURL}/${queryId}")
    GET(url).setHeaders(httpHeaders).apply.onComplete {
      case Success(response) =>  {
        val body = response.toJValue() \ "body"
        val jsonJVal: JValue = parse(body.values.toString)
        val queryStats = jsonJVal \\ "queryStats"
        callback(queryStats.values.asInstanceOf[Map[String, Any]])
      }
      case Failure(e) => throw e
    }
  }

  /**
   * Get all the nodes in the cluster
   * @param callback This callback gets called for every node in the cluster
   */
  def getAllNodes(callback: PrestoNode => Unit) = {
    val url = new URL(s"${nodeURL}")
    GET(url).setHeaders(httpHeaders).apply.onComplete {
      case Success(response) =>  {
        val body = response.toJValue() \ "body"
        val nodeArray: JArray = parse(body.values.toString).asInstanceOf[JArray]
        nodeArray.values.map(node => node.asInstanceOf[HashMap[String, Any]]).
        map(values => new PrestoNode(values("uri").asInstanceOf[String], values("recentRequests").asInstanceOf[Double],
          values("recentFailures").asInstanceOf[Double], values("recentSuccesses").asInstanceOf[Double],
          ISODateTimeFormat.dateTime().parseDateTime(values("lastRequestTime").asInstanceOf[String]),
          ISODateTimeFormat.dateTime().parseDateTime(values("lastResponseTime").asInstanceOf[String]),
          values("recentFailureRatio").asInstanceOf[Double], Duration.valueOf(values("age").asInstanceOf[String]), values("recentFailuresByType").asInstanceOf[Map[String, Double]]))
        .foreach(prestoNode => callback(prestoNode))
      }
      case Failure(e) => throw e
    }
  }
}
