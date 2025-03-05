package com.gu.footballtimemachine

import software.amazon.awssdk.services.s3.S3Client

import java.time.format.DateTimeFormatter
import java.time.{ Instant, ZoneId, ZonedDateTime }
import software.amazon.awssdk.auth.credentials.{ AwsCredentialsProviderChain, InstanceProfileCredentialsProvider, ProfileCredentialsProvider }
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3ClientBuilder
import software.amazon.awssdk.services.s3.model.{ GetObjectRequest, ListObjectVersionsRequest, PutObjectRequest }

import java.nio.charset.StandardCharsets
import scala.beans.BeanProperty
import scala.io.Source

class ApiGatewayResponse(
  @BeanProperty var statusCode: Int,
  @BeanProperty var headers: java.util.Map[String, String],
  @BeanProperty var body: String)

object ApiGatewayResponse {
  import collection.JavaConverters._
  def apply(statusCode: Int, headers: Map[String, String], body: String): ApiGatewayResponse = {
    new ApiGatewayResponse(statusCode, headers.asJava, body)
  }
}

class ApiGatewayRequest {
  @BeanProperty var httpMethod: String = _
  @BeanProperty var path: String = _
  @BeanProperty var queryStringParameters: java.util.Map[String, String] = _
  @BeanProperty var headers: java.util.Map[String, String] = _
  @BeanProperty var body: String = _
  @BeanProperty var base64Encoded: Boolean = false
  @BeanProperty var stageVariables: java.util.Map[String, String] = _
  @BeanProperty var requestContext: ApiGatewayRequestContext = _

  import collection.JavaConverters._
  private def asScalaMap[K, V](m: java.util.Map[K, V]): Map[K, V] = Option(m).map(_.asScala.toMap).getOrElse(Map.empty)
  def queryStringParamMap: Map[String, String] = asScalaMap(queryStringParameters)
  def headerMap: Map[String, String] = asScalaMap(headers)

  override def toString = s"ApiGatewayRequest(" +
    s"httpMethod = $httpMethod, " +
    s"path = $path, " +
    s"queryStringParameters = $queryStringParamMap, " +
    s"headers = $headerMap, " +
    s"base64Encoded = $base64Encoded, " +
    s"stageVariables = $stageVariables, " +
    s"requestContext = $requestContext  " +
    s"body = $body)"
}
class ApiGatewayRequestContext {
  @BeanProperty var stage: String = _

  override def toString = s"ApiGatewayRequestContext(stage = $stage)"
}

object ApiLambda {

  val credentials = AwsCredentialsProviderChain.builder().credentialsProviders(
    ProfileCredentialsProvider.create("mobile"),
    InstanceProfileCredentialsProvider.create()).build()

  val s3Client = S3Client.builder()
    .credentialsProvider(credentials)
    .region(Region.EU_WEST_1)
    .build()

  val bucket = "pa-football-time-machine"

  import collection.JavaConverters._

  def getPaData(request: ApiGatewayRequest): ApiGatewayResponse = {
    val pathItems = request.path.drop(1).split("/")
    pathItems.update(2, "apiKey")
    val s3Path = pathItems.mkString("/")
    println(s"accessing $s3Path")

    val currentTime = computeTime

    val versions = s3Client.listObjectVersions(ListObjectVersionsRequest.builder().bucket(bucket).prefix(s3Path).build()).versions().asScala.toList.sortBy(_.lastModified)

    val version = versions.find(v => v.lastModified().toEpochMilli() > currentTime).getOrElse(versions.last).versionId

    val gor = GetObjectRequest.builder.bucket(bucket).key(s3Path).versionId(version).build()

    println(s"requesting version $version")

    val s3Object = s3Client.getObject(gor)

    println(s"Got version ${s3Object.response().versionId()} dated ${s3Object.response().lastModified()}")

    val content = Source.fromInputStream(s3Object).mkString
    ApiGatewayResponse(200, Map("Content-Type" -> "application/xml"), content)
  }

  def setDate(request: ApiGatewayRequest): ApiGatewayResponse = {
    val startDate = ZonedDateTime.parse(request.queryStringParameters.get("startDate")).toInstant.toEpochMilli
    val offset = System.currentTimeMillis() - startDate
    s3Client.putObject(
      PutObjectRequest.builder().bucket(bucket).key("startDate").build(),
      RequestBody.fromString(startDate.toString, StandardCharsets.UTF_8))
    s3Client.putObject(
      PutObjectRequest.builder().bucket(bucket).key("offset").build(),
      RequestBody.fromString(offset.toString, StandardCharsets.UTF_8))

    val speed = request.queryStringParamMap.getOrElse("speed", 5)
    s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key("speed").build(), RequestBody.fromString(speed.toString, StandardCharsets.UTF_8))

    val date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(computeTime), ZoneId.of("Europe/London"))
    ApiGatewayResponse(200, Map.empty, body = s"""{"currentDate":"${date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"}""")
  }

  def computeTime: Long = {
    val offsetObject = s3Client.getObject(
      GetObjectRequest.builder().bucket(bucket).key("offset").build())

    val offset = Source.fromInputStream(offsetObject).mkString.toLong

    val startDateObject = s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key("startDate").build())
    val startDate = Source.fromInputStream(startDateObject).mkString.toLong

    val speedObject = s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key("speed").build())
    val speed = Source.fromInputStream(speedObject).mkString.toInt
    (System.currentTimeMillis() - startDate - offset) * speed + startDate
  }

  def getTime(request: ApiGatewayRequest): ApiGatewayResponse = {
    val date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(computeTime), ZoneId.of("Europe/London"))
    ApiGatewayResponse(200, Map.empty, body = s"""{"currentDate":"${date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"}""")
  }

  def main(args: Array[String]): Unit = {
    val req = new ApiGatewayRequest()
    req.setHttpMethod("GET")
    req.setPath("/competitions/matchDay/apiKey/20241204")
    req.setQueryStringParameters(Map("startDate" -> "2017-06-11T21:00:00Z", "speed" -> "3").asJava)
//    val resp = setDate(req)
//      val resp = getPaData(req)

    val resp = getTime(req)

    println(resp.body)
  }
}
