/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.awssnsstub.controllers

import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.awssnsstub.controllers.sns.{CreatePlatformEndpoint, PublishRequest, SnsAction}
import uk.gov.hmrc.awssnsstub.repository.SnsSentMessageRepository
import uk.gov.hmrc.awssnsstub.support.ControllerSpec

import scala.concurrent.Future
import scala.language.postfixOps
import scala.xml.Elem

@RunWith(classOf[JUnitRunner])
class SnsControllerSpec extends ControllerSpec with MockitoSugar {

  val sentMessageRepository = mock[SnsSentMessageRepository]
  val forcedFailureNamePart = "_FORCE_A_BAD_REQUEST_"

  private val controller = new SnsController(sentMessageRepository, forcedFailureNamePart)
  private val url = routes.SnsController.handleRequest().url

  private def postRequestData(data: Seq[(String, String)]) = {
    FakeRequest("POST", url)
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
      .withFormUrlEncodedBody(data: _*)
  }

  def contentAsXml(resultFuture: Future[Result]): Elem = {
    scala.xml.XML.loadString(contentAsString(resultFuture))
  }

  "SnsController CreatePlatformEndpoint Actions" should {

    "respond with 200 for and persist the message" in {
      when(sentMessageRepository.insert(any[SnsAction], any[Boolean])).thenReturn(Future(mock[WriteResult]))
      val endpoint = CreatePlatformEndpoint("applicationArn", "registrationToken")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "CreatePlatformEndpoint",
        "Version" -> "2010-03-31",
        "PlatformApplicationArn" -> endpoint.applicationArn,
        "Token" -> endpoint.registrationToken)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe OK
      contentAsXml(resultFuture) mustBe CreatePlatformEndpointResponse(endpoint).success

      verify(sentMessageRepository).insert(endpoint, isFailure = false)
    }

    "respond with a 400 error response given a 'forced failure' token" in {
      when(sentMessageRepository.insert(any[SnsAction], any[Boolean])).thenReturn(Future(mock[WriteResult]))
      val endpoint = CreatePlatformEndpoint("applicationArn", s"some-$forcedFailureNamePart-token")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "CreatePlatformEndpoint",
        "Version" -> "2010-03-31",
        "PlatformApplicationArn" -> endpoint.applicationArn,
        "Token" -> endpoint.registrationToken)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe BAD_REQUEST
      contentAsXml(resultFuture) mustBe CreatePlatformEndpointResponse(endpoint).failure

      verify(sentMessageRepository).insert(endpoint, isFailure = true)
    }

    "respond with 500 if persisting CreatePlatformEndpoint Actions fails" in {
      when(sentMessageRepository.insert(any[SnsAction], any[Boolean])).thenReturn(Future.failed(new RuntimeException("Error!")))
      val endpoint = CreatePlatformEndpoint("applicationArn", "registrationToken")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "CreatePlatformEndpoint",
        "Version" -> "2010-03-31",
        "PlatformApplicationArn" -> endpoint.applicationArn,
        "Token" -> endpoint.registrationToken)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe INTERNAL_SERVER_ERROR
    }
  }

  "SnsController Publish Actions" should {
    "respond with 200 for Publish Actions and persist the message" in {
      when(sentMessageRepository.insert(any[SnsAction], any[Boolean])).thenReturn(Future(mock[WriteResult]))
      val publish = PublishRequest("Tax is fun!", "targetArn")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "Publish",
        "Version" -> "2010-03-31",
        "TargetArn" -> publish.targetArn,
        "Message" -> publish.message)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe OK
      contentAsXml(resultFuture) mustBe PublishRequestResponse(publish).success

      verify(sentMessageRepository).insert(publish, isFailure = false)
    }

    "respond with a 400 error response given a 'forced failure' token" in {
      when(sentMessageRepository.insert(any[SnsAction], any[Boolean])).thenReturn(Future(mock[WriteResult]))
      val publish = PublishRequest("Tax is fun!", s"/some/$forcedFailureNamePart/arn")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "Publish",
        "Version" -> "2010-03-31",
        "TargetArn" -> publish.targetArn,
        "Message" -> publish.message)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe BAD_REQUEST
      contentAsXml(resultFuture) mustBe PublishRequestResponse(publish).failure

      verify(sentMessageRepository).insert(publish, isFailure = true)
    }

    "respond with 500 if persisting Publish Actions fails" in {
      when(sentMessageRepository.insert(any[SnsAction], any[Boolean])).thenReturn(Future.failed(new RuntimeException("Error!")))
      val publish = PublishRequest("Tax is fun!", "targetArn")

      val data: Seq[(String, String)] = Seq(
        "Action" -> "Publish",
        "Version" -> "2010-03-31",
        "TargetArn" -> publish.targetArn,
        "Message" -> publish.message)

      val resultFuture = call(controller.handleRequest(), postRequestData(data))

      status(resultFuture) mustBe INTERNAL_SERVER_ERROR
    }
  }

  "SnsController" should {
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

