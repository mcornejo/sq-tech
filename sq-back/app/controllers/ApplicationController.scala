package controllers

import akka.actor.ActorSystem
import filters.WebhookAction
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.cache._
import play.api.libs.json.{JsNumber, JsObject}
import play.api.libs.ws.WSClient
import services.{EmailService, HttpService, LogService, SlackService}
import targets.{Email, HTTP, Log, SlackMessage, TargetConfig}
import scala.concurrent.{ExecutionContext, Future}
import scala.io.{BufferedSource, Source}
import scala.util.Failure

@Singleton
class ApplicationController @Inject()(val ws: WSClient,
                                      val cache: AsyncCacheApi,
                                      val controllerComponents: ControllerComponents,
                                      val whAction: WebhookAction,
                                      val emailService: EmailService,
                                      val httpService: HttpService,
                                      val logService: LogService,
                                      val slackService: SlackService,
                                     )(implicit context: ExecutionContext, system: ActorSystem) extends BaseController with Logging {
  /*
   * Main method that handles a request, verifies the HMAC and dispatches the message to the different services.
   * */
  def webhook(): Action[RawBuffer] =  whAction.andThen(whAction.AuthenticateCheckAction)(parse.raw).async { implicit request =>
    // Current body of the request
    val body: String = request.body.asBytes().map(_.utf8String).getOrElse("")

    // targetsConfig is the list of all registered targets stored in the cache
    val targetsConfig: Future[List[TargetConfig]] = cache.get[List[TargetConfig]]("targets").map(_.getOrElse(Nil))

    // Selection of service depending on the elements in the target list.
    targetsConfig.map(targets => targets.map {
      case email: Email =>
        logger.info(s"Sending Email - Config ${email}")
        emailService.dispatch(body, email)

      case http: HTTP =>
        logger.info(s"Sending Rquest - Config ${http}")
        httpService.dispatch(body, http)

      case log: Log =>
        logger.info(s"Logging - Config ${log}")
        logService.dispatch(body, log)

      case slack: SlackMessage =>
        logger.info(s"Sending Slack Message - Config ${slack}")
        slackService.dispatch(body, slack)

      case _ =>
        logger.error(s"No service matches the configuration")
        Future { Failure(new Exception("Service Not Found")) }
    }).map(list => Ok(JsObject(Seq("executed_tasks" -> JsNumber(list.size)))))
  }

 /*
  * An auxiliary method to read the logs and display them on the browser
  */
  def readLogs(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val logs: BufferedSource = Source.fromFile("./logs/application.log")
    val lines: String = try logs.mkString finally logs.close()
    logger.info(s"Read ${lines.lines().count()} lines from the logs")
    Ok(lines)
  }

 /*
  * A home just for completeness' sake
  */
  def home(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok
  }

}
