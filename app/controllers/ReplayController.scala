package controllers

import play.api.mvc.{Action, Controller}
import common.{ExecutionContexts, LocalDisk, Slugs}

object ReplayController extends Controller with Slugs with LocalDisk with ExecutionContexts {
  def replay(path: String) = Action.async { implicit request =>
    path.split("/").toList match {
      case DateSlugRegex(date) :: TimeSlugRegex(time) :: slugs => {
        val filepath = slugsToFilePath(slugs)
        for {
          response <- loadFile(s"$date/$time/$filepath.xml")
        } yield Ok(response).as(XML)
      }
      case _ => throw new RuntimeException("Include desired date and time")
    }
  }
}
