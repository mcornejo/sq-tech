package targets

/*
 * Class that holds a slack message target
 * */

case class SlackMessage(url: String) extends TargetConfig

object SlackMessage {
  def apply(url: Option[String]): Option[SlackMessage] = {
    url.map(address => SlackMessage(address))
  }
}
