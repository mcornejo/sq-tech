package services

import targets.{Get, HTTP, Post}
import javax.inject.Inject
import play.api.libs.ws.WSClient
import utils.Crypto

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

// Service in charge to create http requests
class HttpService @Inject() (implicit ec: ExecutionContext, ws: WSClient) extends Target {
  def dispatch(message: String, data: HTTP): Future[Try[String]] = {
    data.method match {
      case Get => ws.url(data.url).addQueryStringParameters("message" -> message).get().map(response => Try(response.status.toString))
      case Post => {
        val hmac = Crypto.calculateHMAC(data.sharedKey, message)
        ws
          .url(data.url)
          .addHttpHeaders("Content-Type" -> "application/json")
          .addHttpHeaders("X-Integrity-Murdix" -> hmac)
          .post(Map("message" -> Seq(message))).map(response => Try(response.status.toString))
      }
    }
  }
}
