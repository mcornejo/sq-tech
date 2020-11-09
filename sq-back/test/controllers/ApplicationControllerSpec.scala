package controllers

import akka.util.ByteString
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{RawBuffer, Result}
import scala.concurrent.duration._
import play.api.test.Helpers._
import play.api.test._
import scala.language.postfixOps
import scala.concurrent.{Await, Future}


class ApplicationControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "ApplicationController" should {

    "returns 200 for home" in {
      val injector = new GuiceApplicationBuilder().injector()
      val controller = injector.instanceOf[ApplicationController]
      val response = controller.home().apply(FakeRequest(GET, "/"))
      status(response) mustBe OK
    }

    "returns 200 for logs" in {
      val injector = new GuiceApplicationBuilder().injector()
      val controller = injector.instanceOf[ApplicationController]
      val response = controller.readLogs().apply(FakeRequest(GET, "/logs"))
      status(response) mustBe OK
    }

    "returns 200 for a correct webhook" in {

      val body = ByteString("[{\"message_id\": null, \"api_version\": \"2\", \"date_created\": \"2020-11-06T13:55:09.503282+00:00\", \"message_type\": \"security_event\", \"retry_count\": 0, \"message\": {\"risk_coefficient\": 25, \"event_category\": \"http_error\", \"event_kind\": \"waf\", \"application_id\": \"5fa41cbdb87595001cb7fd03\", \"application_name\": \"my-express-app\", \"environment\": \"development\", \"date_occurred\": \"2020-11-06T13:35:47.487000+00:00\", \"event_id\": \"5fa551335922d9000f88ecbf\", \"event_url\": \"https://my.sqreen.com/application/5fa41cbdb87595001cb7fd03/events/5fa551335922d9000f88ecbf\", \"humanized_description\": \"Attack tentative from 127.0.0.1\", \"ips\": [{\"address\": \"127.0.0.1\", \"is_tor\": false, \"geo\": {}, \"date_resolved\": \"2020-11-06T13:35:47.514000+00:00\"}]}}]")
      val injector = new GuiceApplicationBuilder().injector()
      val controller = injector.instanceOf[ApplicationController]
      val responseFuture: Future[Result] = controller.webhook().apply(FakeRequest(POST, "/")
        .withHeaders("X-Sqreen-Integrity" -> "2069da86a9c3067d10eb8d2d14aea1cc48b9db624dba7f2264ef2f32d450a22e")
        .withBody(RawBuffer(1000,null,body)))

      val response = Await.result(responseFuture, 10 seconds)
      val bodyResponse = Await.result(response.body.consumeData, 10 seconds).toArray

      status(responseFuture) mustBe OK
      new String(bodyResponse) mustBe "{\"executed_tasks\":0}"
    }

    "returns 403 for an incorrect webhook" in {

      val body = ByteString("[{\"message_id\": ")
      val injector = new GuiceApplicationBuilder().injector()
      val controller = injector.instanceOf[ApplicationController]
      val responseFuture: Future[Result] = controller.webhook().apply(FakeRequest(POST, "/")
        .withHeaders("X-Sqreen-Integrity" -> "2069da86a9c3067d10eb8d2d14ae")
        .withBody(RawBuffer(1000,null,body)))

      status(responseFuture) mustBe FORBIDDEN
    }
  }
}
