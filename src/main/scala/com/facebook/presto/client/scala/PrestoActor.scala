package com.facebook.presto.client.scala

import akka.actor.Actor
import com.facebook.presto.client.QueryResults
import com.facebook.presto.client.scala.messages.{SUBMIT_QUERY, QUERY_RESULTS}
import com.typesafe.scalalogging.slf4j.LazyLogging

package messages {

/**
 * Message for submitting queries to the Presto actor
 * @param SQL query
 */
  case class SUBMIT_QUERY (query:String)

/**
 * Message that the Presto actor sends back to the client
 * as it receives the pages of the query results
 * @param query result pages
 */
  case class QUERY_RESULTS (results:QueryResults)
}

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 * Users can submit queries to the Presto actor with the [[com.facebook.presto.client.scala.messages.SUBMIT_QUERY]] message.
 * Upon receiving the message the actor submits the query to the server and then
 * sends the pages of the query results with [[com.facebook.presto.client.scala.messages.QUERY_RESULTS]] messages to the sender.
 * see [[com.facebook.presto.client.samples.ClientActor]] for a sample client implementation
 * @param config client configuration
 */
class PrestoActor(config: PrestoClientConfig) extends Actor with LazyLogging {
  val prestoClient = new PrestoClient(config)

  def receive = {
    case SUBMIT_QUERY (query) => {
      val s = sender
      prestoClient.submitQuery(query,
        queryResults => {
          s ! QUERY_RESULTS(queryResults) //send results back to the sender
        }
      )
    }
    case _ => logger.warn("Unknown message")
  }
}
