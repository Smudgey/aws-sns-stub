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

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.awssnsstub.repository.SnsSentMessageRepository
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

@Singleton
class SnsTestEndpointsController @Inject()(snsActionRepository: SnsSentMessageRepository) extends BaseController with SnsActionBinding {

  def getLatestSentMessage() = Action.async { implicit request =>
    snsActionRepository.findLatestMessage().map {
      case Some(message) => Ok(Json.toJson(message.action))
      case None => NotFound("No messages have been sent.")
    }
  }

  def deleteAllSentMessages() = Action.async { implicit request =>
    snsActionRepository.removeAll().map(wr => Ok("Messages deleted."))
  }
}

