package controllers

import targets.{Email, HTTP, Log, SlackMessage, TargetConfig}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request}
import play.api.cache._
import play.api.libs.json.JsValue
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TargetController @Inject()(val cache: AsyncCacheApi,
                                 val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {

  // Returns the list of backends installed
  def listTargets(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val targets: Future[List[TargetConfig]] = cache.get[List[TargetConfig]]("targets").map(_.getOrElse(Nil))
    targets.map(target => Ok(target.toString))
  }

  // Remove all elements of the target list
  def cleanTargets(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    cache.removeAll().map(_ => Ok)
  }

  // Parses the body of the request and adds a new backend
  def addTarget(): Action[JsValue] = Action(parse.json).async { implicit request: Request[JsValue] =>
    // We retrieve the type of target
    val targetType: String = ( request.body \ "type").as[String]

    targetType match {
      case "log" => {
        val name: Option[String] = ( request.body \ "name").asOpt[String]
        val level: Option[String] = ( request.body \ "level").asOpt[String]
        val log: Option[Log] = Log(name, level)

        log match {
          case Some(log) =>
            cache
              .get[List[TargetConfig]]("targets")
              .map(_.getOrElse(Nil))
              .map(xs => cache.set("targets", xs :+ log)).map(_ => Ok)

          case _ =>
            Future { BadRequest("Incorrect Parameters") }
        }
      }
      case "email" => {
        val to: Option[String] = ( request.body \ "to").asOpt[String]
        val email: Option[Email] = Email(to)

        email match {
          case Some(email) =>
            cache
              .get[List[TargetConfig]]("targets")
              .map(_.getOrElse(Nil))
              .map(xs => cache.set("targets", xs :+ email)).map(_ => Ok)
          case _ =>
            Future { BadRequest }
        }
      }
      case "http" => {
        val url: Option[String] = ( request.body \ "url").asOpt[String]
        val method: Option[String] = ( request.body \ "method").asOpt[String]
        val sharedKey: Option[String] = ( request.body \ "shared_key").asOpt[String]
        val http: Option[HTTP] = HTTP(url, method, sharedKey)

        http match {
          case Some(http) =>
            cache
              .get[List[TargetConfig]]("targets")
              .map(_.getOrElse(Nil))
              .map(xs => cache.set("targets", xs :+ http)).map(_ => Ok)
          case _ =>
            Future { BadRequest }
        }
      }
      case "slack" => {
        val url: Option[String] = ( request.body \ "url").asOpt[String]
        val slackMessage: Option[SlackMessage] = SlackMessage(url)
        slackMessage match {
          case Some(slack) =>
            cache
              .get[List[TargetConfig]]("targets")
              .map(_.getOrElse(Nil))
              .map(xs => cache.set("targets", xs :+ slack)).map(_ => Ok)
          case _ =>
            Future { BadRequest }
        }
      }
      case _ => Future { BadRequest("Type not found") }
    }
  }
}
