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

