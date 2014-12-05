package com.facebook.presto.client.scala

import com.facebook.presto.client.QueryResults
import com.fasterxml.jackson.databind.ObjectMapper
import com.stackmob.newman.response.HttpResponse

/**
 * Author: Nezih Yigitbasi (nezih.yigitbasi@gmail.com)
 */
object QueryResultsMapper {
  val mapper = new ObjectMapper

  def getQueryResults(httpResponse: HttpResponse) : QueryResults = mapper.readValue(httpResponse.bodyString, classOf[QueryResults])
}
