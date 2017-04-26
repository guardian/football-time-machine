package com.gu.footballtimemachine

import java.io.{ ByteArrayInputStream, InputStream }

import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }
import com.amazonaws.services.s3.model.{ ObjectMetadata, PutObjectRequest }
import com.amazonaws.util.StringUtils
import org.joda.time.DateTime
import pa.MatchDay

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble

/**
 * This is compatible with aws' lambda JSON to POJO conversion.
 * You can test your lambda by sending it the following payload:
 * {"name": "Bob"}
 */
class LambdaInput() {
  var name: String = _
  def getName(): String = name
  def setName(theName: String): Unit = name = theName
}

object Lambda {

  val bucket = "pa-football-time-machine"
  val configuration: Configuration = new Configuration()

  /*
   * This is your lambda entry point
   */
  def handler(lambdaInput: LambdaInput, context: Context): Unit = {
    implicit val logger = context.getLogger
    logger.log(s"Starting in ${configuration.stage}")
    Await.result(process(), 30.seconds)
    logger.log("Done")
  }

  def process()(implicit logger: LambdaLogger): Future[Unit] = {
    val paFootballClient = new PaFootballClient(configuration.paApiKey, configuration.paHost)
    val result = for {
      matches <- paFootballClient.aroundToday
      filteredMatches = matches.filter(inProgress)
    } yield {
      filteredMatches.foreach { theMatch =>
        for {
          matchInfo <- paFootballClient.matchInfoString(theMatch.id)
          matchEvents <- paFootballClient.matchEventsString(theMatch.id)
        } yield {
          putFile(s"match/info/apiKey/${theMatch.id}", matchInfo)
          putFile(s"match/events/apiKey/${theMatch.id}", matchEvents)
        }
      }
    }
    result.map(_ => paFootballClient.terminate)
  }

  def putFile(key: String, content: String)(implicit logger: LambdaLogger): Unit = {
    val contentBytes = content.getBytes(StringUtils.UTF8)

    val is: InputStream = new ByteArrayInputStream(contentBytes)
    val metadata = new ObjectMetadata()
    metadata.setContentType("application/xml")
    metadata.setContentLength(contentBytes.length)

    logger.log(s"uploading to $key")
    configuration.s3Client.putObject(new PutObjectRequest(bucket, key, is, metadata))
  }

  private def inProgress(m: MatchDay): Boolean =
    m.date.isBefore(DateTime.now) && m.date.plus(3 * 60 * 60 * 1000).isAfter(DateTime.now)
}

object TestIt {
  def main(args: Array[String]): Unit = {
    println(Lambda.process()(new LambdaLogger {
      override def log(string: String): Unit = println(string)
    }))
  }
}
