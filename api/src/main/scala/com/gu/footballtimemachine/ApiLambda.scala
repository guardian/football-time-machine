package com.gu.footballtimemachine

import java.time.ZonedDateTime
import java.util.Date

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{ AWSCredentialsProviderChain, DefaultAWSCredentialsProviderChain }
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }

import scala.beans.BeanProperty
import scala.io.Source

class ApiGatewayResponse(
  @BeanProperty var statusCode: Int,
  @BeanProperty var headers: java.util.Map[String, String],
  @BeanProperty var body: String
)

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

  val credentials = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("mobile"),
    DefaultAWSCredentialsProviderChain.getInstance()
  )

  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withCredentials(credentials)
    .withRegion(Regions.EU_WEST_1)
    .build()

  val bucket = "pa-football-time-machine"

  import collection.JavaConverters._

  def getPaData(request: ApiGatewayRequest): ApiGatewayResponse = {
    println(request.path)

    val pathItems = request.path.drop(1).split("/")
    pathItems.update(2, "apiKey")
    val s3Path = pathItems.mkString("/")
    println(s3Path)

    val currentTime = computeTime

    val versions = s3Client.listVersions(bucket, s3Path).getVersionSummaries.asScala.toList
    val version = versions.find(_.getLastModified.getTime < currentTime).getOrElse(versions.head).getVersionId

    val gor = new GetObjectRequest(bucket, s3Path)
    gor.setVersionId(version)

    println(s"requesting version $version")

    val s3Object = s3Client.getObject(gor)

    println(s"Got version ${s3Object.getObjectMetadata.getVersionId} dated ${s3Object.getObjectMetadata.getLastModified}")

    val content = Source.fromInputStream(s3Object.getObjectContent).mkString
    ApiGatewayResponse(200, Map("Content-Type" -> "application/xml"), content)
  }

  def setDate(request: ApiGatewayRequest): ApiGatewayResponse = {
    val startDate = ZonedDateTime.parse(request.queryStringParameters.get("startDate")).toInstant.toEpochMilli
    val offset = System.currentTimeMillis() - startDate
    s3Client.putObject(bucket, "startDate", startDate.toString)
    s3Client.putObject(bucket, "offset", offset.toString)

    val speed = request.queryStringParamMap.getOrElse("speed", 5)
    s3Client.putObject(bucket, "speed", speed.toString)

    ApiGatewayResponse(200, Map.empty, body = s"""{"currentDate":"${new Date(computeTime)}"}""")
  }

  def computeTime: Long = {
    val offsetObject = s3Client.getObject(bucket, "offset")
    val offset = Source.fromInputStream(offsetObject.getObjectContent).mkString.toLong

    val startDateObject = s3Client.getObject(bucket, "startDate")
    val startDate = Source.fromInputStream(startDateObject.getObjectContent).mkString.toLong

    val speedObject = s3Client.getObject(bucket, "speed")
    val speed = Source.fromInputStream(speedObject.getObjectContent).mkString.toInt
    (System.currentTimeMillis() - startDate - offset) * speed + startDate
  }

  def getTime(request: ApiGatewayRequest): ApiGatewayResponse = {
    ApiGatewayResponse(200, Map.empty, body = s"""{"currentDate":"${new Date(computeTime)}"}""")
  }

  def main(args: Array[String]): Unit = {
    val req = new ApiGatewayRequest()
    req.setHttpMethod("GET")
    req.setPath("/match/info/secretKey/3914704")
    req.setQueryStringParameters(Map("startDate" -> "2017-06-11T21:00:00Z", "speed" -> "3").asJava)
    //val resp = getPaData(req)
    //val resp = setOffset(req)
    val resp = getTime(req)

    println(resp.body)
  }
}
