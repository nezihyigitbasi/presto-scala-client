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

import java.net.URI
import java.util.concurrent.atomic.AtomicReference

import com.facebook.presto.client.QueryResults
import com.stackmob.newman.response.HttpResponse
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.concurrent.{Await, Future}

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 *
 * Cursor implementation to retrieve the query results from
 * the server.
 */
class PrestoCursor(queryString: String, prestoClient: PrestoClient) extends Iterable[QueryResults] with LazyLogging{
  require(queryString != null)
  require(prestoClient != null)

  private var nextURI: URI = null
  private[scala] val queryInProgress = new AtomicReference[Option[Future[HttpResponse]]](None)
  private val queryId = new AtomicReference[Option[String]](None)

  def getQueryId() : Option[String] = {
    queryId.get
  }

  override def iterator: Iterator[QueryResults] = new PrestoIterator

  class PrestoIterator extends Iterator[QueryResults] {
    override def hasNext: Boolean = nextURI != null  || queryInProgress.get.isDefined

    /** Fetch the next page of results from the server
      * in a blocking manner.
      * @return the next page of results
      */
    override def next(): QueryResults = {
      val queryResults = getQueryResults
      nextURI = queryResults.getNextUri
      if (queryResults.getError != null) {
        cancelQuery()
        throw new Exception(s"Query failed: ${queryResults.getError}")
      }
      queryId.get match {
        case None => {
          queryId.set(Some(queryResults.getId))
        }
        case Some(s) => ; //do nothing
      }
      queryResults
    }

    private def getQueryResults = {
      queryInProgress.get match {
        case None => Await.result(prestoClient.next(nextURI), prestoClient.config.timeout)
        case Some(s) => {
          val response = Await.result(queryInProgress.get.get, prestoClient.config.timeout)
          queryInProgress.set(None)
          QueryResultsMapper.getQueryResults(response)
        }
      }
    }

    /** Cancels the query
      */
    def cancelQuery() = {
      queryId.get match {
        case Some(s) => {
          prestoClient.delete(queryId.get.get)
          logger.info(s"Canceled query: ${queryId.get.get}")
        }
        case None => ; //do nothing
      }
    }
  }
}