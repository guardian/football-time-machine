package com.gu.footballtimemachine

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, DefaultAWSCredentialsProviderChain}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.gu.conf.{ConfigurationLoader, SSMConfigurationLocation}
import com.gu.{AppIdentity, AwsIdentity}
import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain => AwsCredentialsProviderChainV2, DefaultCredentialsProvider => DefaultCredentialsProviderV2, ProfileCredentialsProvider => ProfileCredentialsProviderV2}

import scala.util.{Failure, Success, Try}

class Configuration {
  val logger = LoggerFactory.getLogger(this.getClass)

  val credentials = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("mobile"),
    DefaultAWSCredentialsProviderChain.getInstance())

  val credentialsv2 = AwsCredentialsProviderChainV2.of(
    ProfileCredentialsProviderV2.builder.profileName("mobile").build,
    DefaultCredentialsProviderV2.create)

  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withCredentials(credentials)
    .withRegion(Regions.EU_WEST_1)
    .build()

  val stack: String = Option(System.getenv("Stack")).getOrElse("mobile")
  val stage: String = Option(System.getenv("Stage")).getOrElse("CODE")
  val app: String = Option(System.getenv("App")).getOrElse("football-time-machine")

  val conf: Try[Config] =
    for {
      identity <- AppIdentity.whoAmI(defaultAppName = app, credentialsv2)
      config <- Try(ConfigurationLoader.load(identity, credentialsv2) {
        case AwsIdentity(app, stack, stage, _) =>
          SSMConfigurationLocation(path = s"/$app/$stage/$stack", region = Regions.EU_WEST_1.toString)
      })
    } yield config

  val config: Config = conf match {
    case Success(config) => {
      logger.info("Successfully loaded configuration")
      config
    }
    case Failure(err) => {
      logger.info("Failed to load configuration")
      throw err
    }
  }

  val paApiKey: String = config.getString("pa.api-key")
  val paHost: String = config.getString("pa.host")

}
