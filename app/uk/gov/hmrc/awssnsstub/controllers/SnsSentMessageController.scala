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
class SnsSentMessageController @Inject()(snsSentMessageRepository: SnsSentMessageRepository) extends BaseController with SnsActionBinding {


  //TODO Add spec to test all the test endpoints below.

  def getLatestMessage() = Action.async { implicit request =>
    snsSentMessageRepository.findLatestMessage().map {
      case Some(message) => Ok(Json.toJson(message.action))
      case None => NotFound("No messages have been sent.")
    }
  }

  def getCreateEndpointMessages(registrationToken: Option[String]) = Action.async { implicit request =>
    registrationToken match {
      case Some(token) => snsSentMessageRepository.findMessages("CreatePlatformEndpoint", Map("registrationToken" -> token))
        .map(results => results.map(_.action))
        .map(results => Ok(Json.toJson(results)))
        .recover {
          case ex: Exception => InternalServerError(ex.getMessage)
        }
      case None => snsSentMessageRepository.findMessages("CreatePlatformEndpoint")
        .map(results => results.map(_.action))
        .map(results => Ok(Json.toJson(results)))
        .recover {
          case ex: Exception => InternalServerError(ex.getMessage)
        }
    }
  }

  def getPublishRequestMessages(targetArn: Option[String]) = Action.async { implicit request =>
    targetArn match {
      case Some(arn) => snsSentMessageRepository.findMessages("PublishRequest", Map("targetArn" -> arn))
        .map(results => results.map(_.action))
        .map(results => Ok(Json.toJson(results)))
        .recover {
          case ex: Exception => InternalServerError(ex.getMessage)
        }
      case None => snsSentMessageRepository.findMessages("PublishRequest")
        .map(results => results.map(_.action))
        .map(results => Ok(Json.toJson(results)))
        .recover {
          case ex: Exception => InternalServerError(ex.getMessage)
        }
    }
  }

  def deleteAllSentMessages() = Action.async { implicit request =>
    snsSentMessageRepository.removeAll().map(_ => Ok("Messages deleted."))
  }
}

