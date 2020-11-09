package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Play.materializer
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.{GET, status}
import play.api.test.{FakeRequest, Injecting}
import play.api.test.Helpers._
import scala.language.postfixOps

class TargetControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "TargetController" should {

    "returns 200 for home" in {
      val injector = new GuiceApplicationBuilder().injector()
      val controller = injector.instanceOf[TargetController]
      val response = controller.cleanTargets().apply(FakeRequest(GET, "/clean"))
      status(response) mustBe OK
    }

    "returns 415 when sending the incorrect content-type" in {
      val injector = new GuiceApplicationBuilder().injector()
      val controller = injector.instanceOf[TargetController]
      val response = controller.addTarget().apply(FakeRequest(POST, "/target")
        .withBody(
          """
            |{"type": "log",
            |"name": "myLoggerDebug",
            |"level": "debug"
            |}""".stripMargin
        )
      )
      status(response) mustBe UNSUPPORTED_MEDIA_TYPE
    }

    "returns 200 when adding a correct target (HTTP)" in {
      val injector = new GuiceApplicationBuilder().injector()
      val controller = injector.instanceOf[TargetController]
      val response = controller.addTarget().apply(FakeRequest(POST, "/target")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(Json.parse("""{"type": "http", "url": "https://webhook.murdix.com", "method": "get", "shared_key": "key"}""")))

      println(contentAsString(response))
      status(response) mustBe OK
    }

    "returns 200 when adding a correct target (Log)" in {
      val injector = new GuiceApplicationBuilder().injector()
      val controller = injector.instanceOf[TargetController]
      val response = controller.addTarget().apply(FakeRequest(POST, "/target")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(Json.parse("""{"type": "log", "name": "myLoggerDebug", "level": "debug"}"""))
      )
      status(response) mustBe OK
    }

    "returns 400 when adding an incorrect target (non_existent)" in {
      val injector = new GuiceApplicationBuilder().injector()
      val controller = injector.instanceOf[TargetController]
      val responseFuture = controller.addTarget().apply(FakeRequest(POST, "/target")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(Json.parse("""{"type": "non_existent", "name": "myLoggerDebug", "level": "debug"}""""))
      )
      status(responseFuture) mustBe BAD_REQUEST
    }
  }
}
