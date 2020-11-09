package utils
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Crypto {

  def calculateHMAC(sharedSecret: String, message: String): String = {
    val secret = new SecretKeySpec(sharedSecret.getBytes("UTF-8"), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(secret)
    val result: Array[Byte] = mac.doFinal(message.getBytes("UTF-8"))
    result.map("%02x" format _).mkString
  }
}
