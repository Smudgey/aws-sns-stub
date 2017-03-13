package uk.gov.hmrc.awssnsstub.controllers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import play.api.test.FakeRequest
import uk.gov.hmrc.awssnsstub.support.FormEncodingUtil.asFormUrlEncoded
import uk.gov.hmrc.awssnsstub.controllers.sns.{CreatePlatformEndpoint, FailedSnsAction, PublishRequest, UnsupportedSnsAction}
import uk.gov.hmrc.play.test.UnitSpec

@RunWith(classOf[JUnitRunner])
class SnsActionBindingSpec extends UnitSpec {

  "SnsActionBinding" should {

    "bind CreatePlatformEndpoint requests" in new SnsActionBinding {

      val data: Seq[(String, String)] = Seq(
        "Action" -> "CreatePlatformEndpoint",
        "Version" -> "2010-03-31",
        "PlatformApplicationArn" -> "arn",
        "Token" -> "token")

      implicit val request =  FakeRequest().withFormUrlEncodedBody(data: _*)

      bind(asFormUrlEncoded(data)) shouldBe CreatePlatformEndpoint("arn", "token")
    }

    "bind Publish requests" in new SnsActionBinding {


      val data: Seq[(String, String)] = Seq(
        "Action" -> "Publish",
        "Version" -> "2010-03-31",
        "TargetArn" -> "targetArn",
        "Message" -> "Tax is fun!")

      implicit val request =  FakeRequest().withFormUrlEncodedBody(data: _*)

      bind(asFormUrlEncoded(data)) shouldBe PublishRequest("Tax is fun!", "targetArn")
    }

    "bind Unknown Action IDs to UnsupportedSnsActions" in new SnsActionBinding {

      val data: Seq[(String, String)] = Seq(
        "Action" -> "ThisAintSomethingWeDo",
        "Version" -> "2010-03-31",
        "TargetArn" -> "targetArn",
        "Message" -> "Tax is fun!")

      implicit val request =  FakeRequest().withFormUrlEncodedBody(data: _*)

      bind(asFormUrlEncoded(data)) shouldBe UnsupportedSnsAction("ThisAintSomethingWeDo")
    }

    "bind requests with NO Action IDs to UnsupportedSnsActions" in new SnsActionBinding {

      val data: Seq[(String, String)] = Seq(
        "Version" -> "2010-03-31",
        "TargetArn" -> "targetArn",
        "Message" -> "Tax is fun!")

      implicit val request =  FakeRequest().withFormUrlEncodedBody(data: _*)

      bind(asFormUrlEncoded(data)) shouldBe FailedSnsAction("No SNS Action Parameter specified in request")
    }
  }
}
