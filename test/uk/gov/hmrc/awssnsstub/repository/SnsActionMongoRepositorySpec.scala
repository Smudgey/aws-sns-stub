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

package uk.gov.hmrc.awssnsstub.repository

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.awssnsstub.controllers.sns.CreatePlatformEndpoint
import uk.gov.hmrc.awssnsstub.support.WithTestApplication
import uk.gov.hmrc.play.test.UnitSpec

class SnsActionMongoRepositorySpec extends UnitSpec with ScalaFutures with WithTestApplication {

  private val snsActionRepository = fakeApplication.injector.instanceOf[SnsSentMessageRepository]

  private def createPlatformEndpoint(id: Int) = CreatePlatformEndpoint(s"endpointArn_$id", s"token_$id")

  "When some SnsAction are stored" should {

    "then the latest should retrievable" in {
      snsActionRepository.insert(createPlatformEndpoint(1))
      snsActionRepository.insert(createPlatformEndpoint(2))
      snsActionRepository.insert(createPlatformEndpoint(3))

      snsActionRepository.findLatestMessage().futureValue.isDefined shouldBe true

      snsActionRepository.findLatestMessage().futureValue.get.action match {
        case cpe: CreatePlatformEndpoint => {
          cpe.applicationArn shouldBe "endpointArn_3"
          cpe.registrationToken shouldBe "token_3"
        }
        case _ => fail()
      }
    }

  }

}
