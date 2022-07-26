package com.gu.footballtimemachine

import java.net.URL
import java.time.LocalDate
import com.amazonaws.services.lambda.runtime.LambdaLogger
import pa._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

import scala.io.Source

class PaFootballClient(override val apiKey: String, apiBase: String)(implicit logger: LambdaLogger) extends PaClient with pa.Http {

  override lazy val base = apiBase

  def GET(urlString: String): Future[Response] = {
    logger.log("Http GET " + urlString.replaceAll(apiKey, "<api-key>"))
    val inputStream = new URL(urlString).openStream()
    val content = Source.fromInputStream(inputStream).getLines().mkString("\n")
    Future.successful(Response(200, content, "OK"))
  }

  override protected def get(suffix: String)(implicit context: ExecutionContext): Future[String] = super.get(suffix)(context)

  def aroundToday: Future[List[MatchDay]] = matchDay(LocalDate.now)

  def matchInfoString(id: String): Future[String] =
    get(s"/match/info/$apiKey/$id").map(interceptErrors)

  def matchEventsString(id: String): Future[String] =
    get(s"/match/events/$apiKey/$id").map(interceptErrors)

  def matchDayString(date: String): Future[String] =
    get(s"/competitions/matchDay/$apiKey/$date").map(interceptErrors)

}
