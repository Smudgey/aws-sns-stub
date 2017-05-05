package uk.gov.hmrc.awssnsstub.controllers

import org.junit.runner.RunWith
import org.mockito.Matchers._
import org.mockito.Mockito.{verify, when}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.api.commands.WriteResult
import reactivemongo.json.ReactiveMongoJsonException
import uk.gov.hmrc.awssnsstub.controllers.sns.{CreatePlatformEndpoint, PublishRequest, SnsAction}
import uk.gov.hmrc.awssnsstub.repository.SnsSentMessageRepository
import uk.gov.hmrc.awssnsstub.support.ControllerSpec

import scala.concurrent.Future
import scala.language.postfixOps
import scala.xml.Elem

@RunWith(classOf[JUnitRunner])
class SnsControllerSpec extends ControllerSpec with MockitoSugar {

  val sentMessageRepository = mock[SnsSentMessageRepository]

  private val controller  = new SnsController(sentMessageRepository)
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

    "respond with 200 for CreatePlatformEndpoint Actions and persist the message" in {
      when(sentMessageRepository.insert(any[SnsAction])).thenReturn(Future(mock[WriteResult]))
      val endpoint = CreatePlatformEndpoint("applicationArn", "registrationToken")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "CreatePlatformEndpoint",
        "Version" -> "2010-03-31",
        "PlatformApplicationArn" -> endpoint.applicationArn,
        "Token" -> endpoint.registrationToken)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe OK
      contentAsXml(resultFuture) mustBe CreatePlatformEndpointResponse(endpoint).success

      verify(sentMessageRepository).insert(endpoint)
    }

    "respond with 200 for Publish Actions and persist the message" in {
      when(sentMessageRepository.insert(any[SnsAction])).thenReturn(Future(mock[WriteResult]))
      val publish = PublishRequest("Tax is fun!", "targetArn")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "Publish",
        "Version" -> "2010-03-31",
        "TargetArn" -> publish.targetArn,
        "Message" -> publish.message)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe OK
      contentAsXml(resultFuture) mustBe PublishRequestResponse(publish).success

      verify(sentMessageRepository).insert(publish)
    }

    "respond with 500 if persisting CreatePlatformEndpoint Actions fails" in {
      when(sentMessageRepository.insert(any[SnsAction])).thenReturn(Future.failed(new RuntimeException("Error!")))
      val endpoint = CreatePlatformEndpoint("applicationArn", "registrationToken")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "CreatePlatformEndpoint",
        "Version" -> "2010-03-31",
        "PlatformApplicationArn" -> endpoint.applicationArn,
        "Token" -> endpoint.registrationToken)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe INTERNAL_SERVER_ERROR
    }

    "respond with 500 if persisting Publish Actions fails" in {
      when(sentMessageRepository.insert(any[SnsAction])).thenReturn(Future.failed(new RuntimeException("Error!")))
      val publish = PublishRequest("Tax is fun!", "targetArn")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "Publish",
        "Version" -> "2010-03-31",
        "TargetArn" -> publish.targetArn,
        "Message" -> publish.message)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe INTERNAL_SERVER_ERROR
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

