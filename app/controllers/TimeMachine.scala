package controllers

import play.api._
import play.api.mvc._
import common.{ExecutionContexts, LocalDisk, Slugs}
import pa.FootballClient


object TimeMachine extends Controller with Slugs with FootballClient with LocalDisk with ExecutionContexts {

  def index = Action {

    val dateTimes = for {
      date <- listFiles("")
      time <- listFiles(date)
    } yield (date, time)

    Ok(views.html.index(dateTimes))
  }

}
