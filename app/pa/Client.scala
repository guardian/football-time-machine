package pa

import common.ExecutionContexts
import scala.concurrent.{ExecutionContext, Future}
import conf.Configuration
import play.api.libs.ws.WS
import org.joda.time.DateMidnight
import play.api.Play._
import scala.Some
import play.Logger
import scala.util.{Failure, Success}
import java.io.File

trait Client extends PaClient with Http with ExecutionContexts {
  def apiCall(suffix: String)(implicit context: ExecutionContext): Future[String] = super.get(suffix)(context)
}

private object Client extends Client {

  override def apiKey: String = Configuration.paKey

  override def GET(urlString: String): Future[pa.Response] = {
    WS.url(urlString).get().map { response =>
      pa.Response(response.status, response.body, response.statusText)
    }
  }

  override val errorHandler: PartialFunction[Throwable, Unit] = {
    case t => {
      t.printStackTrace()
    }
  }
}
private object TestClient extends Client {
  override def GET(urlString: String): Future[Response] = ???

  override def get(suffix: String)(implicit context: ExecutionContext): Future[String] = {
    val todayString = DateMidnight.now().toString("yyyyMMdd")
    val filename = {
      suffix
        .replace("/", "__")
        .replace(todayString, "TODAY")
    }
    val realApiCallPath = {
      suffix
        .replace("KEY", Client.apiKey)
    }

    current.getExistingFile(s"/test/testdata/$filename.xml") match {
      case Some(file) => {
        val xml = scala.io.Source.fromFile(file, "UTF-8").getLines().mkString
        Future(xml)(context)
      }
      case None => {
        Logger.warn(s"Missing fixture for API response: $suffix")
        val response = super.get(realApiCallPath)(context)
        response.onComplete {
          case Success(str) => {
            Logger.info(s"writing response to testdata, $filename.xml, $str")
            writeToFile(s"${current.path}/test/testdata/$filename.xml", str)
          }
          case Failure(writeError) => throw writeError
        }(context)
        response
      }
    }
  }

  def writeToFile(path: String, contents: String): Unit = {
    val writer = new java.io.PrintWriter(new File(path))
    try writer.write(contents) finally writer.close()
  }

  override def apiKey: String = "KEY"
}
trait FootballClient {
  lazy val client = {
    if (play.Play.isTest) TestClient
    else Client
  }
}
