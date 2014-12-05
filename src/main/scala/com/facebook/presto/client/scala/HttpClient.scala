package com.facebook.presto.client.scala

import java.util.concurrent.{Executors, ThreadFactory}

import com.stackmob.newman.ApacheHttpClient

import scala.concurrent.ExecutionContext

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 */
object HttpClientFactory {
  //see https://github.com/stackmob/newman/issues/72
  val factory = new ThreadFactory {
    override def newThread(run: Runnable) = {
      val t = new Thread(run)
      t.setDaemon(true)
      t
    }
  }

  def createApacheHttpClient(threadCount: Int) : ApacheHttpClient = {
    implicit val ctx = ExecutionContext.fromExecutorService(
      Executors.newFixedThreadPool(threadCount, factory))
    new ApacheHttpClient()
  }
}
