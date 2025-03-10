package com.gu.footballtimemachine

import java.io.{ByteArrayInputStream, InputStream}
import java.time.{LocalDate, ZonedDateTime}
import java.time.format.DateTimeFormatter
import com.amazonaws.services.lambda.runtime.{Context, LambdaLogger}
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import pa.MatchDay
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.utils.StringUtils

import java.nio.charset.{Charset, StandardCharsets}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble
import scala.jdk.CollectionConverters.MapHasAsJava

class LambdaInput

object ArchiveLambda {

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
    val today = LocalDate.now.format(DateTimeFormatter.BASIC_ISO_DATE)
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
    paFootballClient.matchDayString(today).map { matchDay =>
      putFile(s"competitions/matchDay/apiKey/$today", matchDay)
    }
    result.recover {
      case e: Exception => logger.log(s"Exception: ${e.getMessage} ${e.getStackTrace.toString}")
    }
  }

  def putFile(key: String, content: String)(implicit logger: LambdaLogger): Unit = {

    val metadata = Map("Content-Type" -> "application/xml")

    logger.log(s"uploading to $key")
    configuration.s3Client.putObject(PutObjectRequest.builder.bucket(bucket).key(key).metadata(metadata.asJava).build(), RequestBody.fromString(content))
  }

  private def inProgress(m: MatchDay): Boolean =
    m.date.isBefore(ZonedDateTime.now.plusMinutes(5)) && m.date.plusHours(3).isAfter(ZonedDateTime.now)
}

object TestIt {
  def main(args: Array[String]): Unit = {
    println(ArchiveLambda.process()(new LambdaLogger {
      override def log(string: String): Unit = println(string)

      override def log(message: Array[Byte]): Unit = println(new String(message, StandardCharsets.UTF_8))
    }))
  }
}
