package targets

/*
 * Class that holds an Email target
 * */

case class Email(to: String)

object Email {
  def apply(to: Option[String]): Option[Email] = {
    to.map(to => Email(to))
  }
}