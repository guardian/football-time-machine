package com.gu.footballtimemachine

import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.amazonaws.services.lambda.runtime.LambdaLogger
import pa._

import scala.concurrent.{ExecutionContext, Future}
import org.joda.time.DateTime

import scala.io.Source

class PaFootballClient(override val apiKey: String, apiBase: String)(implicit logger: LambdaLogger) extends PaClient with pa.Http {

  import ExecutionContext.Implicits.global

  override lazy val base = apiBase

  def GET(urlString: String): Future[Response] = {
    logger.log("Http GET " + urlString.replaceAll(apiKey, "<api-key>"))
    val inputStream = new URL(urlString).openStream()
    val content = Source.fromInputStream(inputStream).getLines().mkString("\n")
    Future.successful(Response(200, content, "OK"))
  }

  override protected def get(suffix: String)(implicit context: ExecutionContext): Future[String] = super.get(suffix)(context)

  def aroundToday: Future[List[MatchDay]] = matchDay(DateTime.now.toLocalDate)

  def matchInfoString(id: String)(implicit context: ExecutionContext): Future[String] =
    get(s"/match/info/$apiKey/$id").map(interceptErrors)

  def matchEventsString(id: String)(implicit context: ExecutionContext): Future[String] =
    get(s"/match/events/$apiKey/$id").map(interceptErrors)

  def matchDayString(date: String)(implicit context: ExecutionContext): Future[String] =
    get(s"/competitions/matchDay/$apiKey/$date").map(interceptErrors)

}
