package services

import targets.{Debug, Error, Info, Log, Warn}
import javax.inject.Inject
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

// Service in charge to log events
class LogService @Inject() ()(implicit ec: ExecutionContext) extends Target {

  def dispatch(message: String, data: Log): Future[Try[String]] = Future {
    val logger: Logger = Logger(data.name)
    Try {
      data.level match {
        case Error =>
          logger.error(message)
          message
        case Warn =>
          logger.warn(message)
          message
        case Info =>
          logger.info(message)
          message
        case Debug =>
          logger.debug(message)
          message
      }
    }
  }
}
