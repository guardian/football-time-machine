package common

import conf.Configuration

trait Slugs {
  val DateSlugRegex = """(201\d[01]\d[0123]\d)""".r
  val TimeSlugRegex = """([0-2][0-9][0-5][0-9])""".r

  def slugsToFilePath(slugs: List[String]): String = {
    slugs.map {
      case DateSlugRegex(dateSlug) => "date"
      case Configuration.paKey => "key"
      case slug => slug.toLowerCase
    }.mkString("/")
  }

  def pathToFilePath(path: String): String = {
    slugsToFilePath(path.split("/").toList)
  }
}
