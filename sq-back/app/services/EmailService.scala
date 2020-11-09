package services

import play.api.libs.mailer._
import targets.{Email => dEmail}
import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

// Service in charge to send emails
class EmailService @Inject() (mailerClient: MailerClient, configuration: Configuration)(implicit ec: ExecutionContext) extends Target {
  def dispatch(message: String, data: dEmail): Future[Try[String]] = Future {
    val email = Email(
      configuration.get[String]("play.mailer.subject"),
      configuration.get[String]("play.mailer.from"),
      Seq(data.to),
      bodyText = Some(message),
      bodyHtml = Some(s"""<html><body><p>$message</p></body></html>""")
    )
    Try(mailerClient.send(email))
  }
}
