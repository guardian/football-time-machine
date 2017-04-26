package com.gu.footballtimemachine

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazonaws.services.lambda.runtime.LambdaLogger
import pa._

import scala.concurrent.{ ExecutionContext, Future }
import org.joda.time.DateTime
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient

class PaFootballClient(override val apiKey: String, apiBase: String)(implicit logger: LambdaLogger) extends PaClient with pa.Http {

  import ExecutionContext.Implicits.global

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val ws: WSClient = AhcWSClient()

  override lazy val base = apiBase

  def GET(urlString: String): Future[Response] = {
    logger.log("Http GET " + urlString.replaceAll(apiKey, "<api-key>"))
    ws.url(urlString).get().map(r => Response(r.status, r.body, r.statusText))
  }

  override protected def get(suffix: String)(implicit context: ExecutionContext): Future[String] = super.get(suffix)(context)

  def aroundToday: Future[List[MatchDay]] = matchDay(DateTime.now.toLocalDate)

  def matchInfoString(id: String)(implicit context: ExecutionContext): Future[String] =
    get(s"/match/info/$apiKey/$id").map(interceptErrors)

  def matchEventsString(id: String)(implicit context: ExecutionContext): Future[String] =
    get(s"/match/events/$apiKey/$id").map(interceptErrors)

  def terminate = {
    ws.close()
    materializer.shutdown()
    system.terminate()
  }
}
