package common

import java.io.File
import play.api.Play._
import scala.concurrent.Future

trait IO {
  def loadFile(path: String): Future[String]
  def writeFile(path: String, contents: String): Unit
  def listFiles(path: String): List[String]
}

trait LocalDisk extends IO {
  val root = s"${current.path}/data/"

  override def loadFile(path: String): Future[String] = {
    val file = new File(s"$root/$path")
    Future.successful(scala.io.Source.fromFile(file).mkString)
  }

  override def writeFile(path: String, contents: String): Unit = {
    val file = new File(s"$root/$path")
    new File(file.getParent).mkdirs()
    val writer = new java.io.PrintWriter(file)
    try {
      writer.write(contents)
    } finally writer.close()
  }

  override def listFiles(path: String): List[String] = {
    val filenames = new File(s"$root/$path").listFiles().toList
      .sortBy(_.getName)
      .map(_.getName)
    filenames
  }
}
