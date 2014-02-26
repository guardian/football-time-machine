package controllers

import play.api.mvc.{Action, Controller}
import common.{ExecutionContexts, LocalDisk, Slugs}
import conf.Configuration
import play.Logger
import org.joda.time.{DateMidnight, DateTime}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import scala.concurrent.Future

object ReplayController extends Controller with Slugs with LocalDisk with ExecutionContexts {
  def replay(path: String) = Action.async { implicit request =>
    path.split("/").toList match {
      case DateSlugRegex(date) :: TimeSlugRegex(time) :: Nil => {
        Future.successful(NotFound(views.html.error("Full path to API endpoint required", "This service reproduces the PA API so you need to browse to the API endpoint you wish to replay.")))
      }
      case DateSlugRegex(date) :: TimeSlugRegex(time) :: slugs => {
        val filepath = slugsToFilePath(slugs)
        val fullPath = s"$date/$time/$filepath.xml"
        for {
          response <- loadFile(fullPath)
        } yield {
          Logger.info(s"${DateTime.now().toString("yyyy/MM/dd HH:mm:ss")} replaying $fullPath")
          val targetDate = DateMidnight.parse(date, DateTimeFormat.forPattern("yyyyMMdd"))
          val responseWithCorrectedDates = rewriteToday(targetDate, response)
          Ok(responseWithCorrectedDates).as(XML)
        }
      }
      case _ => throw new RuntimeException("Include desired date and time")
    }
  }

  private def rewriteToday(targetDate: DateMidnight, response: String): String = {
    response.replaceAllLiterally(
      targetDate.toString("dd/MM/yyyy"),
      DateTime.now().toString("dd/MM/yyyy")
    )
  }
}
