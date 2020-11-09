package filters

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.Results.Forbidden
import play.api.mvc.{ActionBuilder, ActionFilter, ActionTransformer, AnyContent, BodyParsers, RawBuffer, Request, Result, WrappedRequest}
import utils.Crypto
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

// A class that represents a particular request (webhook request)
class WebhookRequest[A](val checksum: Option[String], request: Request[A]) extends WrappedRequest[A](request)

// The transformer that takes a raw Request and enriches it into a WebhookRequest. It also verifies the HMAC.
class WebhookAction @Inject() (val parser: BodyParsers.Default,
                               configuration: Configuration)(implicit val executionContext: ExecutionContext)
                               extends ActionBuilder[WebhookRequest, AnyContent] with ActionTransformer[Request, WebhookRequest] with Logging {

  val sharedSecret: String = configuration.get[String]("sqreen.hmacSecret")

  // Request transformer
  def transform[A](request: Request[A]): Future[WebhookRequest[A]] = Future.successful {
    new WebhookRequest(request.headers.get("X-Sqreen-Integrity"), request)
  }

  // HMAC verifier
  def AuthenticateCheckAction(implicit ec: ExecutionContext): ActionFilter[WebhookRequest] = new ActionFilter[WebhookRequest] {
    def executionContext: ExecutionContext = ec
    def filter[A](input: WebhookRequest[A]): Future[Option[Result]] = Future.successful {

      val body: String = input.body.asInstanceOf[RawBuffer].asBytes().map(_.utf8String).getOrElse("")
      val hmac: String = Crypto.calculateHMAC(sharedSecret, body)

      logger.info(s"Received HMAC: ${input.checksum.getOrElse("")}. Calculated HMAC: ${hmac}")

      if (!input.checksum.contains(hmac)) {
        logger.info("Request's HMACs does not match")
        Some(Forbidden)
      } else
        None
    }
  }
}
