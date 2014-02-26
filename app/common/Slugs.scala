package common

import java.io.File
import play.api.Play._
import conf.Configuration

trait Slugs {
  val DateSlugRegex = """(201\d[01]\d[012]\d)""".r
  val TimeSlugRegex = """([0-2][0-9][0-5][0-9])""".r

  def slugsToFilePath(slugs: List[String]): String = {
    slugs.map {
      case DateSlugRegex(dateSlug) => "DATE"
      case Configuration.paKey => "KEY"
      case slug => slug
    }.mkString("/")
  }

  def pathToFilePath(path: String): String = {
    slugsToFilePath(path.split("/").toList)
  }
}
