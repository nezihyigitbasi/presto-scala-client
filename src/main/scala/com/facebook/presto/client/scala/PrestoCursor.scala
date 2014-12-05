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
  private val queryId = new AtomicReference[Option[String]](None)
  private[scala] val queryInProgress = new AtomicReference[Option[Future[HttpResponse]]](None)

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