package com.gu.footballtimemachine

import java.io.{ BufferedReader, InputStreamReader }
import java.util.stream.Collectors

import com.amazonaws.auth.{ AWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider, InstanceProfileCredentialsProvider }
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }
import com.typesafe.config.{ Config, ConfigFactory }

class Configuration {

  val credentials = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider(),
    new ProfileCredentialsProvider("mobile"),
    InstanceProfileCredentialsProvider.getInstance()
  )

  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withCredentials(credentials)
    .withRegion(Regions.EU_WEST_1)
    .build()

  val stack: String = Option(System.getenv("Stack")).getOrElse("mobile")
  val stage: String = Option(System.getenv("Stage")).getOrElse("CODE")
  val app: String = Option(System.getenv("App")).getOrElse("DEV")

  private val conf: Config = {
    val dataStream = s3Client.getObject("mobile-notifications-dist", s"PROD/football/football.conf").getObjectContent
    val data = new BufferedReader(new InputStreamReader(dataStream)).lines.collect(Collectors.joining("\n"))
    ConfigFactory.parseString(data)
  }

  val paApiKey: String = conf.getString("pa.api-key")
  val paHost: String = conf.getString("pa.host")

}
