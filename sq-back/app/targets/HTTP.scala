package targets

sealed trait Method
case object Get extends Method
case object Post extends Method


/*
 * Class that holds a HTTP target
 * */

case class HTTP(url: String, method: Method, sharedKey: String) extends TargetConfig

object HTTP {
  def apply(url: Option[String], method: Option[String], sharedKey: Option[String]): Option[HTTP] = {

    val methodObj: Option[Method] = method.map(_.toLowerCase()) match {
      case Some("get") => Some(Get)
      case Some("post") => Some(Post)
      case _ => None
    }

    val http = for {
      urlStr <- url
      methodStr <- methodObj
      sKeyStr <- sharedKey
    } yield HTTP(urlStr, methodStr, sKeyStr)

    http
  }
}