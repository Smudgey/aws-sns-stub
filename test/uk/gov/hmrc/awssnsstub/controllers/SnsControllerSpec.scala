package uk.gov.hmrc.awssnsstub.controllers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.libs.Scala
import uk.gov.hmrc.awssnsstub.controllers.sns.CreatePlatformEndpoint
import uk.gov.hmrc.awssnsstub.support.ControllerSpec
import uk.gov.hmrc.play.test.WithFakeApplication

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.xml.Elem

@RunWith(classOf[JUnitRunner])
class SnsControllerSpec extends ControllerSpec with WithFakeApplication {

  private val controller  = new SnsController
  private val url         = routes.SnsController.handleRequest().url


  def contentAsXml(resultFuture: Future[Result]) : Elem = {
    scala.xml.XML.loadString(contentAsString(resultFuture))
  }

  "SnsController" should {

    "respond with 200 CreatePlatformEndpoint" in {

      val data: Seq[(String, String)] = Seq(
        "Action" -> "CreatePlatformEndpoint",
        "Version" -> "2010-03-31",
        "PlatformApplicationArn" -> "applicationArn",
        "Token" -> "registrationToken")

      val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest("POST", url)
          .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
          .withFormUrlEncodedBody(data: _*)

      val resultFuture: Future[Result] = call(controller.handleRequest(), postRequest)

      status(resultFuture)
      val result = Await.result(resultFuture, 1 second)
      result.header.status mustEqual OK


      contentAsXml(resultFuture) mustBe CreatePlatformEndpointResponse(
        CreatePlatformEndpoint("applicationArn", "registrationToken")
      ).success
    }
  }
}

