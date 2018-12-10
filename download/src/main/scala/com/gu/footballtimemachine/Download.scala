package com.gu.footballtimemachine

import java.io.{ File, PrintWriter }
import java.text.SimpleDateFormat

import com.amazonaws.auth.{ AWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider, InstanceProfileCredentialsProvider }
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }

import collection.JavaConverters._
import scala.concurrent.duration.DurationDouble
import scala.concurrent.{ Await, Future }
import scala.xml._
import scala.concurrent.ExecutionContext.Implicits.global

object Download {

  val credentials = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider(),
    new ProfileCredentialsProvider("mobile"),
    InstanceProfileCredentialsProvider.getInstance()
  )

  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withCredentials(credentials)
    .withRegion(Regions.EU_WEST_1)
    .build()

  val bucket = "pa-football-time-machine"

  def download(matchId: String): Future[Unit] = Future {

    val sdf = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss")
    val xmlPP = new PrettyPrinter(500, 2)

    List("info", "events").foreach { fileType =>
      val s3Path = s"match/$fileType/apiKey/$matchId"
      val versions = s3Client.listVersions(bucket, s3Path).getVersionSummaries.asScala.toList.sortBy(_.getLastModified)

      versions.foreach { version =>

        val gor = new GetObjectRequest(bucket, s3Path)
        gor.setVersionId(version.getVersionId)

        val s3Object = s3Client.getObject(gor)

        val content = xmlPP.format(XML.load(s3Object.getObjectContent))

        val dateFormat = sdf.format(version.getLastModified)

        val file = new File(s"files/$matchId/$dateFormat-$fileType.xml")
        file.getParentFile.mkdirs()

        val writer = new PrintWriter(file.getAbsolutePath)
        writer.write(content)
        writer.close()

        println(s"Downloaded $file")
      }
    }
  }

  def main(args: Array[String]): Unit = {
    Await.result(Future.traverse(args.toList)(download), 10.minutes)
  }
}
