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
