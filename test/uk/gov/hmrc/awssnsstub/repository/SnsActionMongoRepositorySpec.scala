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

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import uk.gov.hmrc.awssnsstub.controllers.sns.{CreatePlatformEndpoint, PublishRequest}
import uk.gov.hmrc.awssnsstub.support.WithTestApplication
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class SnsActionMongoRepositorySpec extends UnitSpec with ScalaFutures with WithTestApplication with BeforeAndAfterEach {

  lazy val snsActionRepository = fakeApplication.injector.instanceOf[SnsSentMessageRepository]

  private def createPlatformEndpoint(id: Int) = CreatePlatformEndpoint(s"endpointArn_$id", s"token_$id")

  private def publishRequest(id: Int) = PublishRequest("Hello world!", s"targetArm_$id")

    override def beforeEach() {
      implicit val patienceConfig = PatienceConfig(scaled(Span(500, Millis)), scaled(Span(15, Millis)))
      await(snsActionRepository.removeAll())
    }

  "When some SnsActions are stored" should {
    "then the latest should retrievable" in {

      val i1 = snsActionRepository.insert(createPlatformEndpoint(1))
      val i2 = snsActionRepository.insert(publishRequest(2))
      val i3 = snsActionRepository.insert(createPlatformEndpoint(3))

      val result = for {
        _ <- i1
        _ <- i2
        _ <- i3
        result <- snsActionRepository.findLatestMessage()
      } yield result

      result.isDefined shouldBe true

      result.get.action match {
        case cpe: CreatePlatformEndpoint => {
          cpe.applicationArn shouldBe "endpointArn_3"
          cpe.registrationToken shouldBe "token_3"
        }
        case _ => fail()
      }
    }

    "then you should be able to find them by type" in {
      val i1 = snsActionRepository.insert(createPlatformEndpoint(1))
      val i2 = snsActionRepository.insert(publishRequest(2))
      val i3 = snsActionRepository.insert(createPlatformEndpoint(3))

      val result = for {
        _ <- i1
        _ <- i2
        _ <- i3
        result <- snsActionRepository.findMessages("PublishRequest")
      } yield result

      result.size shouldBe 1
      result(0).action match {
        case pr: PublishRequest => {
          pr.message shouldBe "Hello world!"
          pr.targetArn shouldBe "targetArm_2"
        }
        case _ => fail()
      }
    }

    "then you should be able to find them by type and message properties" in {
      val i1 = snsActionRepository.insert(createPlatformEndpoint(1))
      val i2 = snsActionRepository.insert(publishRequest(2))
      val i3 = snsActionRepository.insert(createPlatformEndpoint(3))

      val result = for {
        _ <- i1
        _ <- i2
        _ <- i3
        result <- snsActionRepository.findMessages("CreatePlatformEndpoint", Map("registrationToken" -> "token_1"))
      } yield result

      result.size shouldBe 1
      result(0).action match {
        case cpe: CreatePlatformEndpoint => {
          cpe.applicationArn shouldBe "endpointArn_1"
          cpe.registrationToken shouldBe "token_1"
        }
        case _ => fail()
      }
    }

  }

}
