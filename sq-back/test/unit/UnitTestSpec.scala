package unit
import org.scalatestplus.play._
import targets._
import utils.Crypto

class UnitTestSpec extends PlaySpec {

  "A proper cryptographic library" must {
    "compute the correct HMAC value" in {
      Crypto.calculateHMAC("1234", "4567") mustBe "9d101d2bf630748679226b767d2031634c520390ff0e926afc09bc65a05bfdb2"
      Crypto.calculateHMAC("key", "The quick brown fox jumps over the lazy dog") mustBe "f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8"
    }
  }

  "Classes" must {
    "be capable to instantiate objects (Email)" in {
      val emailRef = Email("to")

      val emailTo = Email(Some("to"))
      val emailEmpty = Email(None)

      emailTo mustBe Some(emailRef)
      emailEmpty mustBe None
    }

    "be capable to instantiate objects (HTTP)" in {
      val httpGetRef = HTTP("urlGet", Get, "sharedKey")
      val httpPostRef = HTTP("urlPost", Post, "sharedKey")

      /* Correct values */
      val httpGet = HTTP(Some("urlGet"), Some("get"), Some("sharedKey"))
      val httpPost = HTTP(Some("urlPost"), Some("post"), Some("sharedKey"))

      /* Incorrect values */
      val httpEmpty = HTTP(None, None, None)
      val httpEmpty1 = HTTP(Some("url"), None, None)
      val httpEmpty2 = HTTP(None, Some("incorrect"), None)
      val httpEmpty3 = HTTP(None, None, Some("key"))
      val httpEmpty4 = HTTP(None, Some("get"), Some("key"))

      httpGet mustBe Some(httpGetRef)
      httpPost mustBe Some(httpPostRef)

      httpEmpty mustBe None
      httpEmpty1 mustBe None
      httpEmpty2 mustBe None
      httpEmpty3 mustBe None
      httpEmpty4 mustBe None
    }

    "be capable to instantiate objects (Log)" in {
      val debugLogRef = Log("name1", Debug)
      val warnLogRef = Log("name2", Warn)

      /* Correct values */
      val debugLog = Log(Some("name1"), Some("debug"))
      val warnLog = Log(Some("name2"), Some("warn"))

      /* Incorrect values */
      val emptyLog = Log(None, None)
      val emptyLog1 = Log(Some("name"), None)
      val emptyLog2 = Log(None, Some("ALL"))
      val emptyLog3 = Log(Some("name1"), Some("ALL"))

      debugLog mustBe Some(debugLogRef)
      warnLog mustBe Some(warnLogRef)

      emptyLog mustBe None
      emptyLog1 mustBe None
      emptyLog2 mustBe None
      emptyLog3 mustBe None
    }

    "be capable to instantiate objects (SlackMessage)" in {
      val msg1Ref = SlackMessage("url1")

      /* Correct values */
      val msg1 = SlackMessage(Some("url1"))

      /* Incorrect values */
      val emptyMsg = SlackMessage(None)

      msg1 mustBe Some(msg1Ref)

      emptyMsg mustBe None
    }

  }

}
