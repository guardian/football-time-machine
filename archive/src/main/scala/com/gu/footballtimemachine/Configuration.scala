package com.gu.footballtimemachine

import java.io.{ BufferedReader, InputStreamReader }
import java.util.stream.Collectors

import com.amazonaws.auth.{ AWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider, InstanceProfileCredentialsProvider }
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.s3.AmazonS3Client
import com.typesafe.config.{ Config, ConfigFactory }

class Configuration {

  val credentials = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider(),
    new ProfileCredentialsProvider("mobile"),
    InstanceProfileCredentialsProvider.getInstance()
  )

  val s3Client: AmazonS3Client = {
    val c = new AmazonS3Client(credentials)
    c.setRegion(Region.getRegion(Regions.EU_WEST_1))
    c
  }

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