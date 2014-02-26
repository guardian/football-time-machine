package controllers

import play.api.mvc.{Action, Controller}
import conf.Configuration
import common.{ExecutionContexts, Slugs, LocalDisk}
import org.joda.time.DateTime
import play.Logger
import pa.FootballClient

object RecordController extends Controller with Slugs with FootballClient with LocalDisk with ExecutionContexts {

  def record(path: String) = Action.async { implicit request =>
    val apiPath = s"/$path"
    val filePath = slugsToFilePath(path.split("/").toList)
    val fResponse = client.apiCall(apiPath)
    fResponse.onSuccess {
      case response => writePaResponse(filePath, response)
    }
    fResponse.map { response =>
      Ok(response).as(XML)
    }
  }

  def writePaResponse(urlString: String, response: String): Unit = {
    val now = DateTime.now()
    val roundedNow = now.withMinuteOfHour(roundDown(now.minuteOfHour().get(), 10))
    val filePath = pathToFilePath(urlString)
    val fullPath = s"${roundedNow.toString("yyyyMMdd")}/${roundedNow.toString("HHmm")}/$filePath.xml"
    Logger.info(s"${now.toString("yyyy/MM/dd HH:mm:ss")} recording $fullPath")
    writeFile(fullPath, response)
  }

  private def roundDown(n: Int, roundTo: Int) = n / roundTo * roundTo
}
