package targets

sealed trait Level
case object Error extends Level
case object Warn extends Level
case object Info extends Level
case object Debug extends Level

/*
 * Class that holds a logging target
 * */

case class Log(name: String, level: Level) extends TargetConfig

object Log {
  def apply(name: Option[String], level: Option[String]): Option[Log] = {

    val levelObj: Option[Level] = level.map(_.toLowerCase()) match {
      case Some("error") => Some(Error)
      case Some("warn") => Some(Warn)
      case Some("info") => Some(Info)
      case Some("debug") => Some(Debug)
      case _ => None
    }

    val log = for {
      nameStr <- name
      lvlStr <- levelObj
    } yield Log(nameStr, lvlStr)

    log
  }
}
