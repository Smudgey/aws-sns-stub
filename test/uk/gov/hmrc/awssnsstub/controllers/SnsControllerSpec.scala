package uk.gov.hmrc.awssnsstub.controllers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.awssnsstub.controllers.sns.{CreatePlatformEndpoint, PublishRequest}
import uk.gov.hmrc.awssnsstub.support.ControllerSpec

import scala.concurrent.Future
import scala.language.postfixOps
import scala.xml.Elem

@RunWith(classOf[JUnitRunner])
class SnsControllerSpec extends ControllerSpec  {

  private val controller  = new SnsController
  private val url         = routes.SnsController.handleRequest().url

  private def postRequestData(data: Seq[(String, String)]) = {
    FakeRequest("POST", url)
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
      .withFormUrlEncodedBody(data:_*)
  }

  def contentAsXml(resultFuture: Future[Result]) : Elem = {
    scala.xml.XML.loadString(contentAsString(resultFuture))
  }

  "SnsController" should {

    "respond with 200 for CreatePlatformEndpoint Actions" in {

      val endpoint = CreatePlatformEndpoint("applicationArn", "registrationToken")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "CreatePlatformEndpoint",
        "Version" -> "2010-03-31",
        "PlatformApplicationArn" -> endpoint.applicationArn,
        "Token" -> endpoint.registrationToken)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe OK
      contentAsXml(resultFuture) mustBe CreatePlatformEndpointResponse(endpoint).success
    }

    "respond with 200 for Publish Actions" in {

      val publish = PublishRequest("Tax is fun!", "targetArn")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "Publish",
        "Version" -> "2010-03-31",
        "TargetArn" -> publish.targetArn,
        "Message" -> publish.message)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe OK
      contentAsXml(resultFuture) mustBe PublishRequestResponse(publish).success
    }

    "respond with 501 NotImplemented for unknown Actions" in {

      val data: Seq[(String, String)] = Seq("Action" -> "ThisAintGonnaWOrk")
      val resultFuture = call(controller.handleRequest(), postRequestData(data))
      status(resultFuture) mustBe NOT_IMPLEMENTED
      contentAsString(resultFuture) mustBe "The SNS Action ThisAintGonnaWOrk has not been implemented"
    }

    "respond with 400 when the Action is malformed" in {

      val data: Seq[(String, String)] = Seq(
        "Action" -> "Publish",
        "Version" -> "2010-03-31")

      val resultFuture = call(controller.handleRequest(), postRequestData(data))
      status(resultFuture) mustBe BAD_REQUEST
      contentAsString(resultFuture) must startWith("PublishRequest request failed to bind")
    }


    "respond with 400 BadRequest for unknown Actions" in {

      val request = FakeRequest("POST", url)
        .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
        .withJsonBody(JsObject(Map("isTaxFun" -> JsString("Y.E.S"))))

      val resultFuture = call(controller.handleRequest(), request)
      status(resultFuture) mustBe BAD_REQUEST
      contentAsString(resultFuture) mustBe "No SNS Action Parameter specified in request"
    }
  }
}

