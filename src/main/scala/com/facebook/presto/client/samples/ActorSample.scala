package com.facebook.presto.client.samples

import com.facebook.presto.client.scala.{PrestoActor}
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.JavaConverters._

import akka.actor.{Props, ActorSystem, Actor, ActorRef}
import com.facebook.presto.client.scala.messages.{SUBMIT_QUERY, QUERY_RESULTS}

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 */
class ClientActor (prestoActor: ActorRef) extends Actor with LazyLogging {
  def receive = {
    case QUERY_RESULTS (queryResults) => {
      if(queryResults.getData != null) {
        queryResults.getData.asScala.map(data => {
          logger.info(data.asScala.mkString(" - "))
        })
      }
      if (queryResults.getNextUri == null) { //no more results
        logger.info("Shutting down actor system")
        context.system.shutdown()
      }
    }
    case SUBMIT_QUERY (query) => {
      prestoActor ! SUBMIT_QUERY(query)
    }
    case _ =>  logger.warn("Unknown message")
  }
}

object ActorSample extends BaseSample with LazyLogging {
  def main(args: Array[String]) {
    val client = createPrestoClient(args)
    val query = args(0)
    val schema = args(1)
    val catalog = args(2)

    logger.info(s"Will run query=${query} on schema=${schema} catalog=${catalog}")
    val system = ActorSystem("PrestoTestActorSystem")
    val prestoActor = system.actorOf(Props(new PrestoActor(client.config)), name = "presto_actor")
    val prestoClientActor = system.actorOf(Props(new ClientActor(prestoActor)), name = "client_actor")
    prestoClientActor ! SUBMIT_QUERY(query)
  }
}

