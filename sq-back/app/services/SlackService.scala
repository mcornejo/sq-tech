package services

import targets.SlackMessage
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

// Service in charge to send a slack message
class SlackService @Inject() (implicit ec: ExecutionContext, configuration: Configuration, ws: WSClient) extends Target {

  def dispatch(message: String, data: SlackMessage): Future[Try[String]] = {
    ws
      .url(data.url)
      .addHttpHeaders("Content-type" -> "application/json")
      .post(Json.obj("text" -> message))
      .map(response => Try(response.status.toString))
  }
}
