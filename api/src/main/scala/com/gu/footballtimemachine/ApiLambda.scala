package com.gu.footballtimemachine

import scala.beans.BeanProperty

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
  def handler(request: ApiGatewayRequest): ApiGatewayResponse = {
    println(request)
    ApiGatewayResponse(200, Map.empty, "OK")
  }
}
