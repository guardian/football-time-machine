package com.gu.footballtimemachine

import java.io.{ BufferedReader, InputStreamReader }
import java.util.stream.Collectors

import com.amazonaws.auth.{ AWSCredentialsProviderChain, DefaultAWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider, InstanceProfileCredentialsProvider }
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }
import com.gu.{ AppIdentity, AwsIdentity }
import com.gu.conf.{ ConfigurationLoader, SSMConfigurationLocation }
import com.typesafe.config.{ Config, ConfigFactory }
import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProviderChain => AwsCredentialsProviderChainV2,
  ProfileCredentialsProvider => ProfileCredentialsProviderV2,
  DefaultCredentialsProvider => DefaultCredentialsProviderV2
}

class Configuration {

  val credentials = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("mobile"),
    DefaultAWSCredentialsProviderChain.getInstance()
  )

  val credentialsv2 = AwsCredentialsProviderChainV2.of(
    ProfileCredentialsProviderV2.builder.profileName("mobile").build,
    DefaultCredentialsProviderV2.create
  )

  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withCredentials(credentials)
    .withRegion(Regions.EU_WEST_1)
    .build()

  val stack: String = Option(System.getenv("Stack")).getOrElse("mobile")
  val stage: String = Option(System.getenv("Stage")).getOrElse("CODE")
  val app: String = Option(System.getenv("App")).getOrElse("football-time-machine")

  val conf = {
    val identity = AppIdentity.whoAmI(defaultAppName = app, credentialsv2)
    ConfigurationLoader.load(identity, credentialsv2) {
      case AwsIdentity(app, stack, stage, _) =>
        SSMConfigurationLocation(path = s"/$app/$stage/$stack", region = Regions.EU_WEST_1.toString)
    }

  }

  val paApiKey: String = conf.getString("pa.api-key")
  val paHost: String = conf.getString("pa.host")

}
